package up.csd.magpie.task;

import com.unionpay.magpie.remoting.support.magpie.MagpieRequest;
import io.netty.channel.ChannelHandlerContext;
import up.csd.async.AsyncContext;
import up.csd.json.codec.Message;

/**
 * Created by Smile on 2018/5/25.
 */
public abstract class Magpie2JsonContext implements AsyncContext {

    public ChannelHandlerContext channelContext;
    public MagpieRequest reqMsg;

    private Message respMsg;

    public Magpie2JsonContext() { }

    public Magpie2JsonContext(ChannelHandlerContext channelContext, MagpieRequest reqMsg) {
        this.channelContext = channelContext;
        this.reqMsg = reqMsg;
    }

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
