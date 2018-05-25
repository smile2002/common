package up.csd.json.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import up.csd.async.AsyncFlow;
import up.csd.async.AsyncTask;
import up.csd.async.AsyncType;
import up.csd.core.*;
import up.csd.json.codec.*;
import up.csd.util.LogIdUtil;
import up.csd.util.StringUtil;
import up.csd.util.ZDogsUtil;
import up.csd.json.client.ClientConfig.SvrConf;


import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Smile on 2018/5/22.
 */
public class AsyncClient {

    Logger logger = Logger.getLogger(AsyncClient.class);

    private static final EventLoopGroup eventLoopGroup;

    private final String dataCenter;
    private final String svrNm;
    private final String host;
    private final int port;
    private Channel bizClientChannel;
    private final String hostAndPort;
    private int maxThreads = 1000;
    private ConnectionStatus status = ConnectionStatus.NOT_AVAILABLE; // 默认链路不可用
    private final int maxReqNum; // 限流
    private AtomicInteger reqNumCount;
    private final int maxPingNum; // 最大心跳检测次数 - 超过则认为链路心跳异常，需隔离
    private AtomicInteger pingNumCount;
    private AtomicInteger timeOutReqCount;
    private final int maxTimeOutReqNum;
    private long lastRespTm = 0L; // 最近一次接收到应答的时间
    private final int idx;

    static {
        eventLoopGroup = new NioEventLoopGroup(0, new NamedThreadFactory("client-io-worker"));

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                SysLogger.info("ShutdownHook triggled for AsyncClient, shutdown the EventLoopGroup for all Clients now.");
                // 关闭线程池并且释放所有的资源
                eventLoopGroup.shutdownGracefully();
                //eventLoopGroup.shutdown();
            }
        }, "ClientShutdownHook"));
    }

    public AsyncClient(String hostAndPort, int idx, SvrConf svrConf) {
        String[] array = hostAndPort.split(":");
        if (array.length != 2) {
            throw new IllegalArgumentException(hostAndPort);
        }
        this.dataCenter = svrConf.getDataCenter();
        this.hostAndPort = hostAndPort;
        this.host = array[0];
        this.port = Integer.parseInt(array[1]);
        this.svrNm = svrConf.getSvrNm();
        this.maxReqNum = svrConf.getMaxReqNum();
        this.maxPingNum = svrConf.getMaxPingNum();
        this.maxTimeOutReqNum = svrConf.getMaxTimeOutReqNum();
        this.idx = idx;

        // 连接失败不影响部署
        try {
            this.connect();
        } catch (Throwable t) {
            SysLogger.error(this.getSvrInfo() + "[connect error!!]", t);
            ZDogsUtil.sendError(ZDogsCode._2002, getSvrInfo());
        }
    }

    private void connect() {
        // 只有链路不可用状态时才允许发起连接操作
        if (this.status != ConnectionStatus.NOT_AVAILABLE) {
            return;
        }

        reqNumCount = new AtomicInteger(0);
        pingNumCount = new AtomicInteger(0);
        timeOutReqCount = new AtomicInteger(0);

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .remoteAddress(new InetSocketAddress(host, port))
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast("MagpieDecoder", new JsonDecoder());
                        ch.pipeline().addLast("JsonEncoder", new JsonEncoder());
                        ch.pipeline().addLast(PromiseFailHandler.INSTANCE); // 统一处理写出数据失败的情况
                        ch.pipeline().addLast("handler", new ClientHandler(AsyncClient.this));
                    }
                });

        // 连接到远程节点，阻塞等待直到连接完成
        SysLogger.info(getSvrInfo() + "[start connecting]");

        bootstrap.connect().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {

                if (future.isSuccess()) {

                    // 连接成功后，状态先置为 2 - 已连接,待启用
                    setStatus(ConnectionStatus.CONNECTED);

                    SysLogger.info(getSvrInfo() + "[connection established]");
                    bizClientChannel = future.channel();
                    bizClientChannel.closeFuture().addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            SysLogger.warn(getSvrInfo() + "[connection is closed!]");
                            setStatus(ConnectionStatus.NOT_AVAILABLE);
                        }
                    });

                    ping();

                } else {
                    SysLogger.warn(getSvrInfo() + "[connection failed!]", future.cause());
                    ZDogsUtil.sendError(ZDogsCode._2002, getSvrInfo());
                }
            }
        });
    }

    public static String defaultDataCenter() {
        return ClientConfig.getInstance().getDefaultDataCenter();
    }

    /**
     * 如果未指定数据中心、组号，则使用默认的数据中心和默认组
     * @param cmd
     * @param reqMap
     * @return
     */
    public static AsyncClient send(String cmd, Map<?, ?> reqMap) {
        RequestId reqId = RequestId.newInstance();
        return send(reqId, cmd, defaultDataCenter(), reqMap);
    }

    public static AsyncClient send(String cmd, String dataCenter, Map<?, ?> reqMap) {
        RequestId reqId = RequestId.newInstance();
        return send(reqId, cmd, dataCenter, reqMap);
    }

    /**
     * 如未指定数据中心，使用_client.json里面默认的数据中心
     * @param reqId
     * @param cmd
     * @param reqMap
     */
    public static AsyncClient send(RequestId reqId, String cmd, Map<?, ?> reqMap) {
        return send(reqId, cmd, defaultDataCenter(), reqMap);
    }

    public static AsyncClient send(RequestId reqId, String cmd, String dataCenter, Map<?, ?> reqMap) {

        Validate.notNull(reqId, "reqId is null.");
        Validate.notNull(cmd, "cmd is null.");
        Validate.notNull(reqMap, "reqMap is null.");

        ClientPool clientPool = ClientPoolFactory.getInstance().getClientPool(cmd, dataCenter);
        final AsyncClient client = clientPool.chooseClient();

        if (client == null) {
            SysLogger.error("No client available for " + cmd + "@" + dataCenter);
            return null;
        }

        ChannelFuture cf = client._send(reqId, cmd, EasyMap.fromMap(reqMap));
        if (cf == null) {
            return null;
        }

        cf.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                Object logId = reqMap.get("__log_id__");
                Validate.notNull(logId, "logId is null in AsyncClient.send");
                LogIdUtil.setMdc(logId.toString());

                if (future.isSuccess()) {
                    SysLogger.debug(client.getSvrInfo() + "send to server[" + cmd + "] succeed.");
                } else {
                    // 如果发送异常，则将客户端对象状态置为不可用，并将计数减1
                    client.setStatus(ConnectionStatus.NOT_AVAILABLE);
                    ZDogsUtil.sendError(ZDogsCode._2005, client.getSvrInfo());
                    SysLogger.error(client.getSvrInfo() + "[send request error.]" + future.cause(), future.cause());

                    AsyncTask task = AsyncFlow.forgetTask(AsyncType.JSON_REQ, reqId);
                    Validate.notNull(task, "task is not found by reqId");
                    task.doFailAsync(future.cause());
                }
                LogIdUtil.removeMdc();
            }
        });

        return client;
    }

    /**
     * 向C服务发送请求
     *
     * @param reqMap
     * @param reqId
     * @return
     */
    private ChannelFuture _send(RequestId reqId, String cmd, EasyMap reqMap) {
        Message req = this.buildRequest(reqId, cmd, reqMap);

        // 如果不是心跳请求，则对请求数进行检查及限流
        if (reqNumCount.incrementAndGet() > maxReqNum) {
            reqNumCount.decrementAndGet();
            SysLogger.error(this.getSvrInfo() + "[can't accept more requests], msg is not sent! "
                    + "cmd=" + cmd + ", reqId=" + reqId + ", msg="
                    + new String(req.content, Message.CHARSET));
            return null;
        }

        logger.info(this.getSvrInfo() + "serverReq[" + cmd + "]: reqId=" + reqId + ", msg="
                + new String(req.content, Message.CHARSET));

        return bizClientChannel.writeAndFlush(req);
    }

    /**
     * 发起心跳
     */
    public void ping() {

        // 如果服务不可用，则不发起心跳
        if (this.status == ConnectionStatus.NOT_AVAILABLE) {
            SysLogger.error(this.getSvrInfo() + "[can't ping, status is " + status.getCode() + "_" + status.getDesc() + "]");
            return;
        }

        if (checkTimeoutCount()) {
            return;
        }

        timeOutReqCount = new AtomicInteger(0);

        // 判断心跳次数是否超过设定的最大心跳次数阈值
        int pingNum = pingNumCount.incrementAndGet();
        if (pingNum > maxPingNum) {
            SysLogger.error(this.getSvrInfo() + "[can't send ping request, maxPingNum is " + maxPingNum + "]");
            // 如果心跳请求未收到应答的个数超过设定的个数，则认为链路异常，隔离链路，等待后续重连
            setStatus(ConnectionStatus.NOT_AVAILABLE);
            return;
        }

        RequestId reqId = new RequestId(ReqIdGenerator.genReqIdArr());
        Message req = this.buildRequest(reqId, "ping", new EasyMap());
        try {
            bizClientChannel.writeAndFlush(req);
        } catch (Exception e) {
            SysLogger.error(this.getSvrInfo() + "[ping error.]", e);
            ZDogsUtil.sendError(ZDogsCode._2003, getSvrInfo());
            setStatus(ConnectionStatus.NOT_AVAILABLE);
        }
    }

    /**
     * 构造请求报文
     * @param reqId
     * @param cmd
     * @param reqMap
     * @return
     */
    private Message buildRequest(RequestId reqId, String cmd, EasyMap reqMap) {
        Message request = new Message();

        // 构造头
        ClientConfig conf = ClientConfig.getInstance();
        request.header.setVersion(conf.getVersion());
        request.header.setMagicNum(conf.getMagicNum());
        request.header.setReqId1(reqId.id1);
        request.header.setReqId2(reqId.id2);
        request.header.setReqId3(reqId.id3);

        if (cmd.equals("ping")) {
            request.isPing = true;
            // 心跳报文头部的第20-24位固定写死536805378
            request.header.setReqId4(536805378);
        } else {
            request.header.setReqId4(reqId.id4);
        }

        // 填写公共字段
        if (!StringUtil.isBlank(conf.getClientId())) {
            reqMap.put("client_id", conf.getClientId());
        }
        if (!reqMap.containsKey(cmd)) {
            reqMap.put("cmd", cmd);
        }

        request.content = reqMap.toJson().getBytes();
        request.header.setBodyLen(request.content.length);

        return request;
    }

    /**
     * 处理心跳结果
     * @param pingResp
     */
    public void processPingRslt(Message pingResp) {
        int currPingNum = pingNumCount.decrementAndGet();
        if (currPingNum < 0 || currPingNum >= maxPingNum) {
            SysLogger.error(this.getSvrInfo() + "[Error Ping Number:" + currPingNum + "]");
            // 如果心跳计数异常，则隔离链路
            setStatus(ConnectionStatus.NOT_AVAILABLE);
            return;
        }

        // 处理心跳应答
        EasyMap pingRespMap = pingResp.toEasyMap();
        String result = pingRespMap.getAsString("result");
        String pongType = pingRespMap.getAsString("pong_type");

        ConnectionStatus oldStatus = this.status;
        if ("0".equals(result) && "0".equals(pongType)) {
            setStatus(ConnectionStatus.AVAILABLE);
            if (oldStatus == ConnectionStatus.AVAILABLE) {
                SysLogger.debug(this.getSvrInfo() + "[链路正常]");
            } else {
                SysLogger.info(this.getSvrInfo() + "[链路已启用]");
            }
        } else if ("0".equals(result) && "1".equals(pongType)) {
            setStatus(ConnectionStatus.UNDER_MAINTENANCE);
            SysLogger.warn(this.getSvrInfo() + "[服务正在升级]");
        } else {
            setStatus(ConnectionStatus.NOT_AVAILABLE);
            SysLogger.error(this.getSvrInfo() + "[心跳异常:" + pingRespMap + "]");
            ZDogsUtil.sendError(ZDogsCode._2004, getSvrInfo());
        }
    }

    private synchronized void setStatus(ConnectionStatus status) {
        this.status = status;
    }

    /**
     * 重连
     */
    public synchronized void reconnect() {
        // 只对链路不可用状态的客户端进行重连
        if (this.status == ConnectionStatus.NOT_AVAILABLE) {
            // 无论如何先调一次关闭
            this.shutdown();
            // 发起连接操作
            this.connect();
        }
    }

    /**
     * 监控客户端当前情况
     */
    public synchronized void monitor() {
        SysLogger.debug(this.getSvrInfo() + "[reqNumCount=" + reqNumCount.get() + ",lastRespTm=" + lastRespTm + ",status=" + status.getCode() + "]" );
    }

    /**
     * 判断客户端是否可用
     * 除了判断链路状态外，还判断了当前处理的请求数是否已经达到上限
     * @return
     */
    public boolean isAvailable() {
        if (!isChannelAvailable()) {
            return false;
        }
        // 检查当前请求数
        if (reqNumCount.get() > maxReqNum) {
            SysLogger.error(getSvrInfo() + "[over maxReqNum " + reqNumCount.get() + "]");
            return false;
        }
        return true;
    }

    /**
     * 判断通道是否可用
     * @return
     */
    public boolean isChannelAvailable() {
        // 判断状态是否正常
        if (this.status != ConnectionStatus.AVAILABLE) {
            SysLogger.warn("client not available " + getSvrInfo() + "_" + status.getCode() + "_" + status.getDesc());
            return false;
        }
        // 检查链接状态
        if (bizClientChannel == null || !bizClientChannel.isActive()) {
            SysLogger.warn(getSvrInfo() + "[client is not active]");
            return false;
        }
        // 检查心跳是否异常
        if (pingNumCount.get() > maxPingNum) {
            SysLogger.warn(getSvrInfo() + "[ping timeout.]");
            return false;
        }
        return true;
    }

    public void refreshRespTm() {
        this.lastRespTm = System.currentTimeMillis();
    }

    /**
     * 并发请求计数减1
     */
    public void decReqNumCount() {
        reqNumCount.decrementAndGet();
    }

    /**
     * 请求超时
     */
    public void timeout() {
        reqNumCount.decrementAndGet();
        timeOutReqCount.incrementAndGet();
        checkTimeoutCount();
    }

    /**
     * 判断链路是否超时
     * @return
     */
    private boolean checkTimeoutCount() {
        // 如果设置了请求超时最大数，则根据超时请求数是否超过设定值判断是否需隔离
        if (maxTimeOutReqNum > 0) {
            // 如果在ping周期内业务请求超时的个数超过设定的阈值，则隔离该节点，后续发起重连
            // 否则则将业务请求超时计数器重置为0，重新计数
            if (timeOutReqCount.get() > maxTimeOutReqNum) {
                SysLogger.error(this.getSvrInfo() + "[timeout request number exceeds the maxTimeOutReqNum(" + this.maxTimeOutReqNum + ")]");
                this.status = ConnectionStatus.NOT_AVAILABLE;
                return true;
            }
        }
        return false;
    }

    private void shutdown() {
        if (bizClientChannel != null) {
            bizClientChannel.close().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (!future.isSuccess()) {
                        SysLogger.error(getSvrInfo() + "[close failed!!]" + future.cause(), future.cause());
                    }
                }
            });
        }
    }
    public int getMaxThreads() {
        return maxThreads;
    }
    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }
    public int getMaxReqNum() {
        return maxReqNum;
    }
    public String getSvrInfo() {
        return "[" + this.dataCenter + "_" + this.svrNm + idx + "_" + this.hostAndPort + "]";
    }
}