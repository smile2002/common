package up.csd.http.handler;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;
import up.csd.http.server.Http2JsonContext;
import up.csd.http.server.UrlHandler;

/**
 * Created by Smile on 2018/5/23.
 */
public class EchoHandler extends UrlHandler {

    @Override
    public String getDefaultUrl() {
        return "/echo";
    }

    @Override
    public void service(Http2JsonContext context) throws Exception {
        ByteBuf buf = context.httpRequest.content();
        String reqContent = buf.toString(CharsetUtil.UTF_8);
        responseText(context, reqContent);
    }
}
