package up.csd.json.server;

import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import up.csd.async.AsyncFlow;
import up.csd.core.*;
import up.csd.json.codec.JsonDecoder;
import up.csd.json.codec.JsonEncoder;
import up.csd.json.codec.Message;
import up.csd.util.MiscUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Smile on 2018/5/21.
 */
public class JsonServer {
    public String name = getClass().getName();
    private final int port;
    private static int maxThreads = 200;
    public Counter counter;
    private final Map<String, AbstractJsonProcessor> processorMapping = new HashMap<>();

    public JsonServer(int port, String name) {
        this.port = port;
        this.name = name;
        counter = new Counter(name);
        /** 默认绑定管理指令处理器 **/
        bindProcessor("at_mng", AdminProcessor.INSTANCE);
    }

    public int getMaxThreads() {
        return maxThreads;
    }
    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    public void bindProcessor(String cmd, AbstractJsonProcessor processor) {
        processor.counter = this.counter;
        processorMapping.put(cmd, processor);
    }

    public void startup() {
        startup(false);
    }
    public void startup(boolean startCounter) {
        EventLoopGroup bossGroup = new NioEventLoopGroup(
                1, new NamedThreadFactory("boss"));
        EventLoopGroup workerGroup = new NioEventLoopGroup(
                maxThreads, new NamedThreadFactory("-worker"));

        try {
            io.netty.bootstrap.ServerBootstrap b = new io.netty.bootstrap.ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 5000)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast("decoder", new JsonDecoder());
                            ch.pipeline().addLast("encoder", new JsonEncoder());
                            ch.pipeline().addLast(PromiseFailHandler.INSTANCE); // 统一处理写出数据失败的情况
                            ch.pipeline().addLast("handler", new JsonHandler(JsonServer.this));
                        }
                    });

            // 监听端口并调用sync()方法阻塞等待直到绑定完成
            ChannelFuture future = b.bind(port).sync();
            SysLogger.info(getClass().getName() + " listening at " + future.channel().localAddress());

            future.channel().closeFuture().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    System.out.println("!!closeFuture.Listener!!");
                    SeverState.upgrade();
                    int maxWait = 30;
                    while (AsyncFlow.timeoutCtrlMap.size() > 0 && maxWait-- > 0) {
                        SysLogger.warn("ChannelCloseFuture is waiting for all AsyncTasks to complete.");
                        MiscUtil.waitFor(1000);
                    }
                    SysLogger.info("Shutdown " + JsonServer.class.getName() + "[" + port + "] now.");
                    MiscUtil.waitFor(1000);
                    // 关闭线程池并且释放所有的资源
                    bossGroup.shutdownGracefully();
                    workerGroup.shutdownGracefully();
                }
            });

            if (startCounter) {
                this.counter.start();
            }

        } catch (Exception e) {
            SysLogger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void process(ChannelHandlerContext ctx, Message reqMsg) {
        AbstractJsonProcessor processor = processorMapping.get(reqMsg.cmd());
        if (processor == null) {
            EasyMap errRespMap = new EasyMap();
            errRespMap.put("result", "-999");
            errRespMap.put("result_string", "no matched processor for cmd: " + reqMsg.cmd());
            Message respMsg = reqMsg.buildRespMessage(errRespMap);
            ctx.writeAndFlush(respMsg);
            return;
        }
        processor.process(ctx, reqMsg);
    }
}
