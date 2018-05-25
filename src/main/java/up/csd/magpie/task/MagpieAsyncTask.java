package up.csd.magpie.task;

import org.apache.commons.lang.Validate;
import up.csd.async.AsyncTask;
import up.csd.async.AsyncType;
import up.csd.async.TaskStatus;
import up.csd.core.EasyMap;
import up.csd.json.client.AsyncClient;
import up.csd.json.codec.RequestId;

/**
 * Created by Smile on 2018/5/25.
 */
public abstract class MagpieAsyncTask extends AsyncTask {

    private String cmd;
    private long reqTime;
    private AsyncClient asyncClient;
    private RequestId reqId = RequestId.newInstance();

    @Override
    public final AsyncType type() {
        return AsyncType.JSON_REQ;
    }

    @Override
    public final RequestId id() {
        return reqId;
    }

    public String cmd() {
        return cmd;
    }


    public void sendReq(String cmd, EasyMap reqMap) {
        sendReq(cmd, reqMap, AsyncClient.defaultDataCenter());
    }
    public void sendReq(String cmd, EasyMap reqMap, String dataCenter) {

        Validate.notNull(cmd, "cmd cannot be null!");
        Validate.notNull(reqMap, "reqMap cannot be null!");
        Validate.notNull(dataCenter, "dataCenter cannot be null!");

        this.named(cmd);
        this.cmd = cmd;

        reqMap.put("__log_id__", this.context().logId());

        rememberMe(); // 异步事件真正发生前将task暂存，等待回调

        reqTime = System.currentTimeMillis();
        this.status = TaskStatus.HALF_DONE;

        asyncClient = AsyncClient.send(this.id(), cmd, dataCenter, reqMap);
        if (asyncClient == null) {
            forgetMe();
            doFailAsync(new RuntimeException("Send uphead req failed, CMD[" + cmd + "]"));
        }
    }
}
