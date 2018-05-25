package up.csd.json.task;

import io.netty.channel.ChannelHandlerContext;
import up.csd.async.AsyncContext;
import up.csd.json.codec.Message;

/**
 * Created by Smile on 2018/5/22.
 */
public abstract class JsonAsyncContext implements AsyncContext {

    public ChannelHandlerContext channelContext;
    public Message reqMsg;
    private Message respMsg;

    public JsonAsyncContext() { }

	public JsonAsyncContext(ChannelHandlerContext channelContext, Message reqMsg) {
        this.channelContext = channelContext;
        this.reqMsg = reqMsg;
    }

    @Override
    public <T> void message(T message) {
        respMsg = (Message) message;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T message() {
        return (T) respMsg;
    }
}
