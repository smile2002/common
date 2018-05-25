package up.csd.json.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.log4j.Logger;
import up.csd.async.AsyncFlow;
import up.csd.async.AsyncType;
import up.csd.core.SysLogger;
import up.csd.json.codec.Message;
import up.csd.json.codec.RequestId;
import up.csd.util.LogIdUtil;

import java.util.Map;

/**
 * Created by Smile on 2018/5/22.
 */
public class ClientHandler extends SimpleChannelInboundHandler<Message> {

    private static final Logger logger = Logger.getLogger(ClientHandler.class);

    private AsyncClient client;

    public ClientHandler(AsyncClient client) {
        this.client = client;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message resp) throws Exception {
        if (resp.isPing) {
            client.processPingRslt(resp);
            return;
        }

        // 并发请求计数减一
        client.decReqNumCount();

        Map<String, String> respMap = resp.toEasyMap().toFlatMap();

        RequestId reqId = resp.header.getReqId();
        String logId = respMap.remove("__log_id__");

        if (logId == null) {
            LogIdUtil.setMdc(resp.header.getReqId().toString());
            SysLogger.warn(client.getSvrInfo() + " serverResp without logId: reqId=" + reqId + ", msg=" + new String(resp.content, Message.CHARSET));
        } else {
            LogIdUtil.setMdc(logId);
        }

        logger.info(client.getSvrInfo() + " serverResp: reqId=" + reqId + ", msg=" + new String(resp.content, Message.CHARSET));

        AsyncFlow.wakeup(AsyncType.JSON_REQ, reqId, resp);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LogIdUtil.removeMdc();
        SysLogger.error("exceptionCaught by ClientHandler, " + cause, cause);
    }

}