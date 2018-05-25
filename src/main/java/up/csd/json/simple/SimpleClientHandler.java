package up.csd.json.simple;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import up.csd.json.codec.Message;
import up.csd.util.JsonUtil;

import java.util.HashMap;
import java.util.Random;

/**
 * Created by Smile on 2018/5/22.
 */
public class SimpleClientHandler extends ChannelInboundHandlerAdapter {

    Random random  = new Random(System.currentTimeMillis());

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Send to server...");
        long l = random.nextLong();
        l = (l<0)?l*-1:l;
        Message msg = new Message();
        msg.header.setVersion(1);
        msg.header.setMagicNum(23456);
        msg.header.setReqId1(3);
        msg.header.setReqId2(4);
        msg.header.setReqId3(5);

        HashMap map = new HashMap<>();
        map.put("cmd", "at_mng");
        map.put("__log_id__", "168434135165313");
        map.put("mng_op", "upgrade");
        String reqBody = JsonUtil.toJson(map);
        msg.content = reqBody.getBytes();
        msg.header.setBodyLen(msg.content.length);

        ctx.writeAndFlush(msg);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //ByteBuf buf = (ByteBuf) msg;// 获取服务端传来的Msg
        Message resp = (Message)msg;

        System.out.println("应答: " + resp.toEasyMap().toString());
        ctx.channel().disconnect();
        ctx.channel().close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelInactive");
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelUnregistered");
    }
}
