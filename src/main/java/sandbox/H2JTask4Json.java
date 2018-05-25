package sandbox;

import up.csd.core.EasyMap;
import up.csd.http.server.Http2JsonContext;
import up.csd.http.server.UrlHandler;
import up.csd.json.codec.Message;
import up.csd.json.task.AsyncTask4Json;

/**
 * Created by Smile on 2018/5/23.
 */
public class H2JTask4Json extends AsyncTask4Json {
        @Override
        public void doo() throws Exception {
            EasyMap req = new EasyMap();
            req.put("laiwjeifjwalf", "wfoiej");
            req.put("key43333", "wfoiej");
            sendReq("bill_no", req);
        }

        @Override
        public void callback() throws Exception {
            Http2JsonContext context = this.context();
            Message respMsg = context.serverResponse();
            String billNo = respMsg.toEasyMap().getAsString("billNo");
            UrlHandler.responseText(context, "Hahahaha! " + billNo);
        }
}
