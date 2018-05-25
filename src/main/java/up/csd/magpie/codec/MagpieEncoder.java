package up.csd.magpie.codec;

import com.unionpay.common.log.Logger;
import com.unionpay.common.util.StringUtil;
import com.unionpay.magpie.common.CompressAlgorithmType;
import com.unionpay.magpie.common.SerializationType;
import com.unionpay.magpie.common.URL;
import com.unionpay.magpie.common.serialize.Serializer;
import com.unionpay.magpie.common.utils.NameableServiceLoader;
import com.unionpay.magpie.remoting.Compressor;
import com.unionpay.magpie.remoting.support.magpie.MagpieHeader;
import com.unionpay.magpie.remoting.support.magpie.MagpieHeartbeatMessage;
import com.unionpay.magpie.remoting.support.magpie.MagpieRequest;
import com.unionpay.magpie.remoting.support.magpie.MagpieResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import up.csd.json.codec.Message;

import java.io.IOException;
import java.util.List;


public class MagpieEncoder extends MessageToMessageEncoder<MagpieRequest> {
    private static final Logger logger = Logger.getLogger(MagpieEncoder.class);

    private URL url;
    private Serializer serializer;
    private Compressor compressor;

    public MagpieEncoder() {
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, MagpieRequest msg, List<Object> out) throws Exception {
        ByteBuf buffer = Unpooled.buffer(20);
        MagpieHeader message = (MagpieHeader)msg;
        byte[] bytes = this.serializeData(message);
        bytes = this.compressData(bytes);
        message.setLength(bytes.length);
        if(logger.isDebugEnabled()) {
            logger.debug(message);
        }

        this.writeHeader(buffer, message);
        buffer.writeBytes(bytes);
        out.add(buffer);
    }


    private void writeHeader(ByteBuf out, MagpieHeader message) throws IOException {
        out.writeShort(message.getMagic());
        out.writeByte((byte)(message.getMajorVersion() << 4 & 240) | message.getMinorVersion() & 15);
        byte flag = 0;
        byte flag1 = (byte)(((message.isHeartbeat()?1:0) & 1) << 2 | flag);
        flag1 = (byte)(((message.isOneWay()?1:0) & 1) << 1 | flag1);
        flag1 = (byte)(((message.isRequest()?1:0) & 1) << 0 | flag1);
        out.writeByte(flag1);
        if(this.isRequest(message)) {
            out.writeByte(0);
        } else {
            out.writeByte(((MagpieResponse)message).getStatus().value());
        }

        out.writeLong(message.getRequestId());
        out.writeBytes(StringUtil.rightPad(message.getCallerId(), 8, "").getBytes());
        out.writeBytes(StringUtil.rightPad(message.getServiceId(), 20, "").getBytes());
        out.writeInt(message.getLength());
    }

    protected byte[] serializeData(MagpieHeader message) {
        return this.isRequest(message)?((MagpieRequest)message).getRequestBytes():((MagpieResponse)message).getResponseBytes();
    }

    private byte[] compressData(byte[] bytes) {
        byte[] result = bytes;
        Compressor compressor = this.getCompressor();
        if(compressor != null) {
            try {
                result = compressor.compress(bytes);
            } catch (IOException var5) {
                logger.error("compress error.", var5);
            }
        }

        return result;
    }

    protected boolean isRequest(Object msg) {
        if(msg instanceof MagpieHeartbeatMessage) {
            return true;
        } else if(msg instanceof MagpieRequest) {
            return true;
        } else if(msg instanceof MagpieResponse) {
            return false;
        } else {
            throw new IllegalArgumentException("msg MUST be MagpieRequest or MagpieResponse, actual: " + msg);
        }
    }

    public MagpieEncoder setUrl(URL url) {
        this.url = url;
        String serializationType = url.getParameter("serializationtype");
        String compressAlgorithm = url.getParameter("compressAlgorithm");
        logger.trace("url: " + url);
        logger.trace("serializationType:" + serializationType);
        logger.trace("compressAlgorithm:" + compressAlgorithm);
        if(!SerializationType.BINARY.getText().equals(serializationType)) {
            this.serializer = (Serializer) NameableServiceLoader.getLoader(Serializer.class).getService(serializationType);
        }

        if(!CompressAlgorithmType.NONE.getText().equals(compressAlgorithm)) {
            this.compressor = (Compressor)NameableServiceLoader.getLoader(Compressor.class).getService(compressAlgorithm);
        }

        return this;
    }

    public String getName() {
        return "magpie_binary";
    }

    public URL getUrl() {
        return this.url;
    }

    public Serializer getSerializer() {
        return this.serializer;
    }

    protected Compressor getCompressor() {
        return this.compressor;
    }
}
