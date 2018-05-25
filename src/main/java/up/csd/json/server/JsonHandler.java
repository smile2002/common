package up.csd.json.server;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.log4j.Logger;
import up.csd.core.SysLogger;
import up.csd.json.codec.Message;
import up.csd.util.LogIdUtil;

/**
 * Created by Smile on 2018/5/22.
 */
@Sharable
public class JsonHandler extends SimpleChannelInboundHandler<Message> {

    private static final Logger logger = Logger.getLogger("sys.log");

    private final JsonServer server;

    public JsonHandler(JsonServer server) {
        this.server = server;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message reqMsg) throws Exception {

        if (reqMsg.isPing) {
            PingProcessor.INSTANCE.process(ctx, reqMsg);
        } else {
            server.counter.incReq();
            if(reqMsg != null) {
                LogIdUtil.setMdc(reqMsg.logId());
            }
            logger.debug("begin proc");
            server.process(ctx, reqMsg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        SysLogger.error("exceptionCaught by JsonHandler, " + cause, cause);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        server.counter.incConn();
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        server.counter.decConn();
        super.channelInactive(ctx);
    }

}