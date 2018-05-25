package up.csd.core;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import org.apache.log4j.Logger;

/**
 * Created by Smile on 2018/5/23.
 */
public class TcpServer {

    protected static final Logger logger = Logger.getLogger(TcpServer.class);

    private final int port;
    private String name = getClass().getName();
    private int maxMessageSize = 100 * 1024;
    private int maxThreads = 200;
    private Class<? extends ChannelInboundHandlerAdapter> messageHandlerClass = DummyEchoHandler.class;
    private ChannelInitializer<SocketChannel> channelInitializer;

    public TcpServer(int port) {
        this.port = port;
    }
    public TcpServer(int port, String name) {
        this.port = port;
        this.name = name;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getMaxMessageSize() { return maxMessageSize; }
    public void setMaxMessageSize(int maxMessageSize) { this.maxMessageSize = maxMessageSize; }
    public int getMaxThreads() { return maxThreads; }
    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    public void setChannelInitializer(ChannelInitializer<SocketChannel> httpServerInitializer) {
        this.channelInitializer = httpServerInitializer;
    }

    public Class<? extends ChannelInboundHandlerAdapter> getMessageHandlerClass() {
        return messageHandlerClass;
    }
    public void setMessageHandlerClass(Class<? extends ChannelInboundHandlerAdapter> messageHandlerClass) {
        this.messageHandlerClass = messageHandlerClass;
    }


    public void startup() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(
                Runtime.getRuntime().availableProcessors() * 16,
                new NamedThreadFactory(this.name + "-boss"));
        EventLoopGroup workerGroup = new NioEventLoopGroup(
                maxThreads, new NamedThreadFactory(this.name + "-worker"));
        logger.info(this.name + " max worker threads num is " + maxThreads);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 5000)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
//				.handler(new LoggingHandler(LogLevel.ERROR))
                    .childHandler(channelInitializer);

            // 监听端口并调用sync()方法阻塞等待直到绑定完成
            ChannelFuture future = b.bind(port).sync();
            logger.info(TcpServer.class.getName() + " listening at " + future.channel().localAddress());

            future.channel().closeFuture().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    logger.info("Shutdown " + TcpServer.class.getName() + "[" + port + "] now.");
                    // 关闭线程池并且释放所有的资源
                    bossGroup.shutdownGracefully();
                    workerGroup.shutdownGracefully();
                }
            });

        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    public class DefaultTcpChannelInitializer extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ch.pipeline().addLast("decoder1", new LengthFieldBasedFrameDecoder(maxMessageSize, 0, 4, 0, 4)); // 4位无符号整形做长度
            ch.pipeline().addLast("decoder2", new StringDecoder(CharsetUtil.UTF_8));

            ch.pipeline().addLast("encoder2", new LengthFieldPrepender(4));
            ch.pipeline().addLast("encoder1", new StringEncoder(CharsetUtil.UTF_8));

            ch.pipeline().addLast("handler", messageHandlerClass.newInstance());
        }
    }

    public static class DummyEchoHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            String message = (String) msg;
            logger.info("Server received: " + message);
            ctx.writeAndFlush(message);
        }
    }

    public static void main(String[] args) throws Exception {

        int port = 3456;

        if (args.length > 1) {
            port = Integer.parseInt(args[0]);
        }

        TcpServer server = new TcpServer(port);
        server.setMessageHandlerClass(DummyEchoHandler.class);
        server.startup();
    }
}