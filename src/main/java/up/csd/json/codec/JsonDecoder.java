package up.csd.json.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.log4j.Logger;
import up.csd.util.ByteUtil;

import java.util.List;


public class JsonDecoder extends ByteToMessageDecoder {

    private static final Logger logger = Logger.getLogger("sys.log");

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        logger.debug("decoder working!");
        Object resp = decode(ctx, in);
        if (resp != null) {
            out.add(resp);
        }
    }

    private Object decode(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
        if (buf.readableBytes() < Header.LENGTH) {
            return null;
        }
        byte[] headerBytes = new byte[Header.LENGTH];
        buf.readBytes(headerBytes);
        Header header = this.parseHeader(headerBytes);

        logger.debug("decoder: bodylen=" + header.getBodyLen());
        logger.debug("decoder: magicnum=" + header.getMagicNum());


        long contentLen = header.getBodyLen();
        if (buf.readableBytes() < contentLen) {
            buf.resetReaderIndex();
            return null;
        }

        Message msg = new Message();
        msg.header = header;
        byte[] content = new byte[(int)contentLen];
        if(contentLen > 0){
            buf.readBytes(content);
        }
        msg.content = content;
        logger.debug("decoder: content=" + new String(msg.content));

        if (536805378 == msg.header.getReqId4()) {
            msg.isPing = true;
        } else {
            msg.isPing = false;
        }

        return msg;
    }


    private Header parseHeader(byte[] headBytes) {
        Header header = new Header();
        header.setId(ByteUtil.bytes2short(headBytes, 0));
        header.setVersion(ByteUtil.bytes2short(headBytes, 2));
        header.setLogId(ByteUtil.bytes2int(headBytes, 4));
        header.setReqId1(ByteUtil.bytes2int(headBytes, 8));
        header.setReqId2(ByteUtil.bytes2int(headBytes, 12));
        header.setReqId3(ByteUtil.bytes2int(headBytes, 16));
        header.setReqId4(ByteUtil.bytes2int(headBytes, 20));
        header.setMagicNum(ByteUtil.bytes2int(headBytes, 24));
        header.setBodyLen(ByteUtil.bytes2int(headBytes, 28));
        return header;
    }
}
