package sandbox;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.util.CharsetUtil;
import up.csd.async.AsyncFlow;
import up.csd.http.server.HttpContext;
import up.csd.http.server.UrlHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Smile on 2018/5/23.
 */
public class H2JHandler extends UrlHandler {
    private final Map<HttpMethod, Map<String, ProcBean>> wxFuncs = new HashMap<>();


    @Override
    public void service(HttpContext context) throws Exception {
        String reqContent = parseContentAsString(context.httpRequest, CharsetUtil.UTF_8);
        AsyncFlow
                .first(new H2JTask())
                .context(context)
                .start();
    }



    private class ProcBean {
        UrlHandler.Func func;
        String procTp;
    }
}
