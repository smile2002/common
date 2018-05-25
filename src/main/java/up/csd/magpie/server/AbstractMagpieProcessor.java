package up.csd.magpie.server;

import com.unionpay.magpie.remoting.support.magpie.MagpieRequest;
import com.unionpay.magpie.remoting.support.magpie.MagpieResponse;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.apache.log4j.Logger;
import up.csd.core.Counter;
import up.csd.core.EasyMap;
import up.csd.util.JsonUtil;
import up.csd.util.LogIdUtil;

/**
 * Created by Smile on 2018/5/25.
 */
public abstract class AbstractMagpieProcessor {
    public static Logger logger = Logger.getLogger(AbstractMagpieProcessor.class);
    public Counter counter;

    public abstract void process(ChannelHandlerContext ctx, EasyMap reqMsg);


    public void sendResp(ChannelHandlerContext ctx, EasyMap respMap, MagpieRequest request, long startTime) {
        logger.info("Resp [" + JsonUtil.toJson(respMap) + "]");
        String logId = LogIdUtil.getFromMdc();

        MagpieResponse response = new MagpieResponse(request);
        response.setResponseBytes(respMap.toJson().getBytes());
        response.setRequestId(request.getRequestId());
        ctx.writeAndFlush(response).addListener(new ChannelFutureListener() {
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
