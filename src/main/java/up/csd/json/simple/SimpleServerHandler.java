package up.csd.json.simple;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import up.csd.core.EasyMap;
import up.csd.json.codec.Message;

/**
 * Created by Smile on 2018/5/22.
 */
public class SimpleServerHandler extends ChannelDuplexHandler {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("连接的客户端地址:" + ctx.channel().remoteAddress());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("断开的客户端地址:" + ctx.channel().remoteAddress());
        super.channelInactive(ctx);
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //ByteBuf req = (ByteBuf) msg;
        //String str = req.toString(Charset.defaultCharset());
        //System.out.println("Server: req=" + str);

        Message req = (Message) msg;
        System.out.println("REQUEST:" + req.toEasyMap().toString());

        EasyMap respMap = new EasyMap();
        respMap.put("result", "0");
        respMap.put("result_string", "Success!");

        Message resp = req.buildRespMessage(respMap);
        //Thread.sleep(4000);
        ctx.writeAndFlush(resp);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Channel incoming = ctx.channel();
        System.out.println("SimpleClient:" + incoming.remoteAddress() + "异常");
        //if (cause instanceof IOException) {
        System.out.println(cause.toString());
        cause.printStackTrace();
        //}
        ctx.close();
    }
}
