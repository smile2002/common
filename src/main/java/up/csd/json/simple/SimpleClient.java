package up.csd.json.simple;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.log4j.Logger;
import up.csd.json.codec.JsonDecoder;
import up.csd.json.codec.JsonEncoder;

import java.util.Random;

/**
 * Created by Smile on 2018/5/21.
 */
public class SimpleClient {

    private static final Logger logger = Logger.getLogger("sys.log");

    public static void main( String[] args )
    {
        Random random = new Random(System.currentTimeMillis());
        System.out.println( "Hi I am client!" );
        SocketChannel socketChannel;
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();

        try {
            Bootstrap clientBootstrap = new Bootstrap();
            clientBootstrap.channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .group(eventLoopGroup)
                    .remoteAddress("127.0.0.1", 9600)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new JsonDecoder())
                                    .addLast(new JsonEncoder())
                                    .addLast(new SimpleClientHandler());
                        }
                    });
            ChannelFuture future = clientBootstrap.connect().sync();
            if (future.isSuccess()) {
                socketChannel = (SocketChannel) future.channel();
                System.out.println("------connect server success------");
            }
            //Channel ch = future.channel();
            //ch.writeAndFlush(String.valueOf(random.nextLong()));
            ChannelFuture closeFuture = future.channel().closeFuture();
            closeFuture.sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }
}