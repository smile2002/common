package up.csd.http.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import up.csd.async.AsyncContext;
import up.csd.core.Counter;
import up.csd.json.codec.Message;
import up.csd.util.LogIdUtil;

/**
 * Created by Smile on 2018/5/23.
 */
public class Http2JsonContext implements AsyncContext {

    public Counter counter;

    public ChannelHandlerContext reqChannel;
    public FullHttpRequest httpRequest;
    public long startTime;
    public String logId;

    private Message reqMsg;

    public Http2JsonContext(ChannelHandlerContext reqChannel, FullHttpRequest request, Counter counter) {
        this.logId = LogIdUtil.genAndSetMdc(LogIdUtil.SYS_ID_RCV);
        this.startTime = System.currentTimeMillis();
        this.reqChannel = reqChannel;
        this.httpRequest = request;
        this.counter = counter;
        counter.incReq();
    }

    @Override
    public String logId() {
        return logId;
    }

    @Override
    public <T> void serverResponse(T message) {
        reqMsg = (Message) message;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T serverResponse() {
        return (T) reqMsg;
    }
}
