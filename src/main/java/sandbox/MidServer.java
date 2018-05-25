package sandbox;

import io.netty.channel.ChannelHandlerContext;
import up.csd.async.AsyncFlow;
import up.csd.core.EasyMap;
import up.csd.json.codec.Message;
import up.csd.json.server.AbstractJsonProcessor;
import up.csd.json.server.JsonServer;
import up.csd.json.task.AsyncTask4Json;
import up.csd.json.task.AsyncContext4Json;
import up.csd.util.LogIdUtil;

/**
 * Created by Smile on 2018/5/22.
 */
public class MidServer {
    public static void main(String[] args) {

        JsonServer server = new JsonServer(9500, "Essential Server");
        server.bindProcessor("consume", new AbstractJsonProcessor() {
            @Override
            public void process(ChannelHandlerContext ctx, Message reqMap) {
                AsyncContext4Json asyncContext = new AsyncContext4Json(ctx, reqMap) {
                    @Override
                    public String logId() {
                        return this.reqMsg.logId();
                    }
                };
                AsyncFlow flow = new AsyncFlow();
                flow.first(new AsyncTask4Json() {
                    @Override
                    public void doo() throws Exception {
                        System.out.println("Execute doo()...");
                        EasyMap reqMap = new EasyMap();
                        reqMap.put("cmd", "bill_no");
                        reqMap.put("para1", "alsfheuf");
                        reqMap.put("__log_id__", LogIdUtil.genId(LogIdUtil.SYS_ID_BIZ));
                        this.sendReq("bill_no", reqMap);
                    }
                    @Override
                    public void callback() throws Exception {
                        AsyncContext4Json ctx = this.context();
                        ctx.serverResponse();
                        EasyMap respMap = new EasyMap();
                        respMap.put("result", "0");
                        respMap.put("result_string", "resp from midsvr");
                        Message respMsg = ctx.reqMsg.buildRespMessage(respMap);
                        ctx.channelContext.writeAndFlush(respMsg);
                    }
                }).context(asyncContext).start();
            }
        });
        server.startup(true);
    }
}
