package sandbox;

import up.csd.core.EasyMap;
import up.csd.http.server.HttpContext;
import up.csd.http.server.UrlHandler;
import up.csd.json.codec.Message;
import up.csd.json.task.JsonAsyncTask;

/**
 * Created by Smile on 2018/5/23.
 */
public class H2JTask extends JsonAsyncTask {
        @Override
        public void doo() throws Exception {
            EasyMap req = new EasyMap();
            req.put("laiwjeifjwalf", "wfoiej");
            req.put("key43333", "wfoiej");
            sendReq("bill_no", req);
        }

        @Override
        public void callback() throws Exception {
            HttpContext context = this.context();
            Message respMsg = context.message();
            String billNo = respMsg.toEasyMap().getAsString("billNo");
            UrlHandler.responseText(context, "Hahahaha! " + billNo);
        }
}
