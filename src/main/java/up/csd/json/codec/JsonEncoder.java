package up.csd.json.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.apache.log4j.Logger;
import up.csd.util.ByteUtil;

import java.util.List;

/**
 * Created by Smile on 2018/5/21.
 */
public class JsonEncoder extends MessageToMessageEncoder<Message> {

    private static final Logger logger = Logger.getLogger("sys.log");

    @Override
    protected void encode(ChannelHandlerContext ctx, Message req, List<Object> out) throws Exception {
        logger.debug("encoder working...");
        // 分配长度
        // 转换报文头
        byte[] headerBytes = new byte[32];
        Header header = req.header;
        ByteUtil.short2bytes(headerBytes, 0, (int) header.getId()); // 2
        ByteUtil.short2bytes(headerBytes, 2, (int) header.getVersion()); // 2
        ByteUtil.int2bytes(headerBytes, 4, (int) header.getLogId()); // 4
        ByteUtil.int2bytes(headerBytes, 8, (int) header.getReqId1()); // 4
        ByteUtil.int2bytes(headerBytes, 12, (int) header.getReqId2()); // 4
        ByteUtil.int2bytes(headerBytes, 16, (int) header.getReqId3()); // 4
        ByteUtil.int2bytes(headerBytes, 20, (int) header.getReqId4()); // 4
        ByteUtil.int2bytes(headerBytes, 24, (int) header.getMagicNum()); // 4
        ByteUtil.int2bytes(headerBytes, 28, (int) header.getBodyLen()); // 4

        logger.debug("encoder content len = " + header.getBodyLen());
        ByteBuf buffer = Unpooled.buffer(Header.LENGTH + (int)header.getBodyLen());

        // 转换报文体
        buffer.writeBytes(headerBytes);
        buffer.writeBytes(req.content);

        logger.debug("encoder content  = " + new String(req.content));
        out.add(buffer);
    }
}