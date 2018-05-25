package up.csd.json.server;

import io.netty.channel.ChannelHandlerContext;
import up.csd.core.EasyMap;
import up.csd.json.codec.Message;

/**
 * Created by Smile on 2018/5/22.
 */
public class PingProcessor extends AbstractJsonProcessor {
    public static final PingProcessor INSTANCE = new PingProcessor();

    @Override
    public void process(ChannelHandlerContext ctx, Message req) {
        EasyMap respMap = new EasyMap();
        respMap.put("result", "0");
        respMap.put("pong_type", SeverState.getPongType());
        ctx.writeAndFlush(req.buildRespMessage(respMap));
    }
}
