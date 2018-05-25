package up.csd.http.handler;

import up.csd.core.Counter;
import up.csd.http.server.HttpContext;
import up.csd.http.server.UrlHandler;

/**
 * Created by Smile on 2018/5/23.
 */
public class CounterResetHandler extends UrlHandler {
    @Override
    public String getDefaultUrl() {
        return "/clear";
    }

    @Override
    public void service(HttpContext context) throws Exception {
        Counter counter = context.counter;
        counter.clear();
        UrlHandler.sendRedirect(context, "/count");
    }
}
