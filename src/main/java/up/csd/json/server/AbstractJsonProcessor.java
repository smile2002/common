package up.csd.json.server;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.apache.log4j.Logger;
import up.csd.core.Counter;
import up.csd.core.EasyMap;
import up.csd.json.codec.Message;
import up.csd.util.JsonUtil;
import up.csd.util.LogIdUtil;

/**
 * Created by Smile on 2018/5/22.
 */
public abstract class AbstractJsonProcessor {
    public static Logger logger = Logger.getLogger(AbstractJsonProcessor.class);
    public Counter counter;

    public abstract void process(ChannelHandlerContext ctx, Message reqMsg);

    public void sendResp(ChannelHandlerContext ctx, EasyMap respMap, Message req, long startTime) {

        logger.info("Resp [" + JsonUtil.toJson(respMap) + "]");
        String logId = LogIdUtil.getFromMdc();

        Message resp = req.buildRespMessage(respMap);
        ctx.writeAndFlush(resp).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                long timeUsed = System.currentTimeMillis() - startTime;
                counter.addTime(timeUsed);

                LogIdUtil.setMdc(logId);
                if (future.isSuccess()) {
                    logger.info("end proc, duration=" + timeUsed);
                } else {
                    logger.warn("end proc, duration=" + timeUsed + ", failed reason=" + future.cause().getMessage(), future.cause());
                }
            }
        });
        LogIdUtil.removeMdc();
    }
}
