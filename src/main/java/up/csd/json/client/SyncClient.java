package up.csd.json.client;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import up.csd.async.AsyncFlow;
import up.csd.core.EasyMap;
import up.csd.json.codec.Message;
import up.csd.json.task.JsonAsyncTask;
import up.csd.json.task.JsonAsyncContext;
import up.csd.util.LogIdUtil;

import java.util.concurrent.TimeUnit;

/**
 * Created by Smile on 2018/5/22.
 */
public class SyncClient {

    private static final Logger logger = Logger.getLogger(SyncClient.class);

    public static EasyMap send(String cmd, EasyMap reqMap) {
        return send(cmd, AsyncClient.defaultDataCenter(), reqMap, 5, TimeUnit.SECONDS);
    }

    public static EasyMap send(String cmd, EasyMap reqMap, int timeout, TimeUnit timeUnit) {
        return send(cmd, AsyncClient.defaultDataCenter(), reqMap, timeout, timeUnit);
    }

    public static EasyMap send(String cmd, String dataCenter, EasyMap reqMap, int timeout, TimeUnit timeUnit) {
        try {
            AsyncFlow flow = AsyncFlow.first(new JsonAsyncTask() {
                @Override
                public void doo() throws Exception {
                    sendReq(cmd, reqMap);
                }
                @Override
                public void callback() throws Exception {
                    // nothing to do here
                }
            }).context(new JsonAsyncContext() {
                @Override
                public String logId() {
                    String logId = reqMap.getAsString("__log_id__");
                    return logId != null ? logId : LogIdUtil.genId(LogIdUtil.SYS_ID_CORE);
                }
            }).defaultTimeout(timeout, timeUnit);

            flow.start().sync();

            Message upHeadMessage = flow.context().message();
            Validate.notNull(upHeadMessage);
            return upHeadMessage.toEasyMap();

        } catch (InterruptedException e) {
            logger.warn(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
