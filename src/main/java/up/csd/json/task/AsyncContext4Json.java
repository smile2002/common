package up.csd.json.task;

import io.netty.channel.ChannelHandlerContext;
import up.csd.async.AsyncContext;
import up.csd.json.codec.Message;

/**
 * Created by Smile on 2018/5/22.
 */
public abstract class AsyncContext4Json implements AsyncContext {

    public ChannelHandlerContext channelContext;
    private Message reqMsg;
    private Message respMsg;

    public AsyncContext4Json() { }

	public AsyncContext4Json(ChannelHandlerContext channelContext, Message reqMsg) {
        this.channelContext = channelContext;
        this.reqMsg = reqMsg;
    }

    @Override
    public <R> void clientRequest(R request) { reqMsg = (Message) request; }

    @SuppressWarnings("unchecked")
    @Override
    public <R> R clientRequest() {return (R) reqMsg; }

    @Override
    public <T> void serverResponse(T message) {
        respMsg = (Message) message;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T serverResponse() {
        return (T) respMsg;
    }
}
