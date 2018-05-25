package up.csd.json.simple;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import up.csd.json.codec.JsonDecoder;
import up.csd.json.codec.JsonEncoder;

/**
 * Created by Smile on 2018/5/21.
 */
public class SimpleServer {
    public static void main( String[] args )
    {
        System.out.println( "Hi I am server!" );
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();


        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            System.out.println("new channel!");
                            ch.pipeline()
                                    .addLast(new JsonDecoder())
                                    .addLast(new JsonEncoder())
                                    .addLast(new SimpleServerHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture f = bootstrap.bind(8099).sync();
            f.channel().closeFuture().sync();
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("shutodwn...");
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}

