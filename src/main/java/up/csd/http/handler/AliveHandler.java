package up.csd.http.handler;

import up.csd.http.server.Http2JsonContext;
import up.csd.http.server.UrlHandler;

/**
 * Created by Smile on 2018/5/23.
 */
public class AliveHandler extends UrlHandler {

    @Override
    public String getDefaultUrl() {
        return "/alive";
    }

    @Override
    public void service(Http2JsonContext context) throws Exception {
        responseText(context, "YES!");
    }
}
