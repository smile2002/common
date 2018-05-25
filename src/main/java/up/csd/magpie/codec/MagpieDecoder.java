package up.csd.magpie.codec;

import com.unionpay.magpie.common.URL;
import com.unionpay.magpie.common.serialize.Serializer;
import com.unionpay.magpie.common.utils.NameableServiceLoader;
import com.unionpay.magpie.common.utils.UrlUtil;
import com.unionpay.magpie.remoting.Compressor;
import com.unionpay.magpie.remoting.log.RemotingLogger;
import com.unionpay.magpie.remoting.log.RemotingMessages;
import com.unionpay.magpie.remoting.support.magpie.MagpieHeader;
import com.unionpay.magpie.remoting.support.magpie.MagpieRequest;
import com.unionpay.magpie.remoting.support.magpie.MagpieResponse;
import com.unionpay.magpie.remoting.support.magpie.MagpieStatus;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;

/**
 * Created by Smile on 2018/5/24.
 */
public class MagpieDecoder extends ByteToMessageDecoder {
    private static final Logger logger = Logger.getLogger("sys.log");
    private URL url;
    private Serializer serializer;
    private Compressor compressor;


    public MagpieDecoder() {

    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        logger.debug("decoder working!");
        Object resp = decode(ctx, in);
        if (resp != null) {
            out.add(resp);
        }
    }

    private Object decode(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
        int readable = buf.readableBytes();
        if (readable < 45) {
            return null;
        }

        buf.markReaderIndex();
        short magic = buf.readShort();
        if(magic != 6939) {
            buf.resetReaderIndex();
            return null;
        }

        byte version = buf.readByte();
        byte flag = buf.readByte();
        byte heartbeatByte = (byte)(flag >> 2 & 1);
        byte onewayByte = (byte)(flag >> 1 & 1);
        byte requestByte = (byte)(flag >> 0 & 1);
        boolean isHeartbeat = heartbeatByte == 1;
        boolean isOneway = onewayByte == 1;
        boolean isRequest = requestByte == 1;
        byte statusByte = buf.readByte();

        MagpieStatus status = MagpieStatus.valueOf(statusByte);
        String callerId;

        if(!isHeartbeat && !isRequest && null == status) {
            buf.resetReaderIndex();
            SocketAddress requestId1 = ctx.channel().remoteAddress();
            String address = "";
            if(requestId1 instanceof InetSocketAddress) {
                address = UrlUtil.getAddressKey((InetSocketAddress)requestId1);
            }

            callerId = "0x" + Integer.toHexString(statusByte);
            RemotingLogger.ROOT_LOGGER.illegalStatusError(callerId, address);
            throw RemotingMessages.MESSAGES.statusException(callerId, address);
        }

        long requestId = buf.readLong();
        callerId = (new String(buf.readBytes(8).array())).trim();
        String serviceId = (new String(buf.readBytes(20).array())).trim();
        int length = buf.readInt();
        if(readable < length + 45) {
            buf.resetReaderIndex();
            return null;
        } else {
            byte[] bytes = new byte[length];
            buf.readBytes(bytes);
            MagpieHeader message = this.createMessage(isRequest);
            message.setMagic(magic);
            message.setMajorVersion((byte)(version >>> 4 & 15));
            message.setMinorVersion((byte)(version >>> 0 & 15));
            message.setHeartbeat(isHeartbeat);
            message.setOneWay(isOneway);
            message.setRequest(isRequest);
            message.setRequestId(requestId);
            message.setCallerId(callerId);
            message.setServiceId(serviceId);
            message.setLength(length);
            if(status != null) {
                message.setStatus(status);
            }

            if(isHeartbeat) {
                logger.debug("Received heartbeat from " + ctx.channel().remoteAddress());
            } else {
                if(logger.isDebugEnabled()) {
                    logger.debug(message);
                }
                bytes = this.uncompressData(bytes);
                this.deserializeData(message, bytes);
            }

            return message;
        }
    }

    protected MagpieHeader createMessage(boolean isRequest) {
        return (MagpieHeader)(isRequest?new MagpieRequest():new MagpieResponse());
    }

    private byte[] uncompressData(byte[] bytes) {
        byte[] result = bytes;
        Compressor compressor = this.getCompressor();
        if(compressor != null) {
            try {
                result = compressor.uncompress(bytes, 0, bytes.length);
            } catch (IOException var5) {
                logger.error("compress error.", var5);
            }
        }

        return result;
    }

    protected void deserializeData(MagpieHeader message, byte[] bytes) {
        if(message instanceof MagpieRequest) {
            ((MagpieRequest)message).setRequestBytes(bytes);
        } else if(message instanceof MagpieResponse) {
            ((MagpieResponse)message).setResponseBytes(bytes);
        }

    }

    public MagpieDecoder setUrl(URL url) {
        this.url = url;
        this.serializer = (Serializer) NameableServiceLoader.getLoader(Serializer.class).getService(url.getParameter("serializationtype"));
        this.compressor = (Compressor)NameableServiceLoader.getLoader(Compressor.class).getService(url.getParameter("compressAlgorithm"));
        return this;
    }

    public Serializer getSerializer() {
        return this.serializer;
    }

    protected Compressor getCompressor() {
        return this.compressor;
    }

}
