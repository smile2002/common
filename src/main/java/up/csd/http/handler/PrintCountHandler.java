package up.csd.http.handler;

import up.csd.core.Counter;
import up.csd.http.server.HttpContext;
import up.csd.http.server.UrlHandler;

/**
 * Created by Smile on 2018/5/23.
 */
public class PrintCountHandler extends UrlHandler {

    @Override
    public String getDefaultUrl() {
        return "/count";
    }

    @Override
    public void service(HttpContext context) throws Exception {
        Counter counter = context.counter;
        StringBuilder buf = new StringBuilder();
        buf.append("Current connections amount is " + counter.CONN_COUNT.get());
        buf.append("\nTotal request count is " + counter.REQ_COUNT.get());
        buf.append("\nPending request count is " + counter.PENDING_COUNT.get());
        buf.append("\nTimeout request count is " + counter.TIMEOUT_COUNT.get());
        buf.append("\nTPS in " + counter.COUNT_PERIOD + " seconds is " + counter.TPS);
        buf.append("\nAVG in " + counter.COUNT_PERIOD + " seconds is " + counter.AVG);
        buf.append("\n\n");
        buf.append(counter.printUriCountMap());
        responseText(context, buf.toString());
    }
}