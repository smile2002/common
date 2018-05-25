package up.csd.core;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import org.apache.log4j.Logger;

/**
 * Created by Smile on 2018/5/22.
 */
@Sharable
public class PromiseFailHandler extends ChannelOutboundHandlerAdapter {

    private Logger logger = Logger.getLogger(PromiseFailHandler.class);

    public static final PromiseFailHandler INSTANCE = new PromiseFailHandler();

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        promise.addListener(future -> {
            if (!future.isSuccess()) {
                logger.warn("write on channel" + ctx.channel() + " failed!" + promise.cause(), promise.cause());
            }
        });
        super.write(ctx, msg, promise);
    }
}
