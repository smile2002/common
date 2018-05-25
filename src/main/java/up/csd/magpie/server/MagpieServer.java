package up.csd.magpie.server;

import com.unionpay.magpie.common.URL;
import com.unionpay.magpie.common.utils.NameableServiceLoader;
import com.unionpay.magpie.remoting.MessageWrapper;
import com.unionpay.magpie.remoting.Request;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import up.csd.async.AsyncFlow;
import up.csd.core.*;
import up.csd.json.server.SeverState;
import up.csd.json.server.JsonServer;
import up.csd.magpie.codec.MagpieDecoder;
import up.csd.magpie.codec.MagpieEncoder;
import up.csd.util.MiscUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Smile on 2018/5/24.
 */
public class MagpieServer {

    public String name = getClass().getName();
    private final int port;
    private static int maxThreads = 200;
    public Counter counter;
    private URL url;
    private final long heartbeatInterval;
    private final long readerIdleTime;


    private final Map<String, AbstractMagpieProcessor> processorMapping = new HashMap<>();
    private static final Map<String, MessageWrapper> heartbeatMap = NameableServiceLoader.getLoader(MessageWrapper.class).getServices();


    public MagpieServer(URL url, int port, String name) {
        this.port = port;
        this.name = name;
        this.heartbeatInterval = Long.parseLong(url.getParameter("heartbeatInterval"));
        this.readerIdleTime = this.heartbeatInterval * 3L;

        counter = new Counter(name);
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
                            ch.pipeline().addLast("magpie_decoder", new MagpieDecoder());
                            ch.pipeline().addLast("magpie_encoder", new MagpieEncoder());
                            MessageWrapper heartbeat = (MessageWrapper)heartbeatMap.get(MagpieServer.this.url.getProtocol());
                            if(heartbeat != null && MagpieServer.this.heartbeatInterval > 0L) {
                                Request heartbeatMessage = heartbeat.wrapHeartbeat();
                            }
                            ch.pipeline().addLast(PromiseFailHandler.INSTANCE); // 统一处理写出数据失败的情况
                            ch.pipeline().addLast("handler", new MagpieHandler(MagpieServer.this) );
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


    public void process(ChannelHandlerContext ctx, byte[] reqBytes) {
        EasyMap reqMap = EasyMap.fromJson(new String(reqBytes));
        String cmd = reqMap.getAsString("cmd");
        AbstractMagpieProcessor processor = processorMapping.get(cmd);
//        if (processor == null) {
//            /** TODO: response meaningful serverResponse **/
//            return;
//        }
        processor.process(ctx, reqMap);
    }
}
