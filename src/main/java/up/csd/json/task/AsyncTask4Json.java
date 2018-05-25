package up.csd.json.task;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import up.csd.async.AsyncTask;
import up.csd.async.AsyncType;
import up.csd.async.TaskStatus;
import up.csd.core.EasyMap;
import up.csd.core.SysLogger;
import up.csd.core.ZDogsCode;
import up.csd.json.client.AsyncClient;
import up.csd.json.codec.RequestId;
import up.csd.util.ZDogsUtil;

/**
 * Created by Smile on 2018/5/22.
 */
public abstract class AsyncTask4Json extends AsyncTask {
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

    @Override
    public final void failTimeout() {
        String svrInfo = StringUtils.EMPTY;
        if (asyncClient == null) {
            SysLogger.warn(nameWithType() + "asyncClient is null?");
        } else {
            svrInfo = asyncClient.getSvrInfo();
        }
        SysLogger.warn(svrInfo + nameWithType() + "AsyncReqTask[" + this.name() + "] failed because of read timeout.");
        ZDogsUtil.sendError(ZDogsCode._2010, this.name());
        asyncClient.timeout();
        readTimeout();
    }

    /**
     * Server超时未响应该请求时的处理方法。<br>
     * 子类可重写该方法。
     */
    public void readTimeout() {
        logger.warn("Read timeout for " + nameWithType());
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

    @Override
    public final void beforeCallback() throws Exception {
        String svrInfo = StringUtils.EMPTY;
        if (asyncClient == null) {
            SysLogger.warn(nameWithType() + "AsyncClient is null?");
        } else {
            svrInfo = asyncClient.getSvrInfo();
        }
        SysLogger.info(svrInfo + nameWithType() + " server duration: " + (System.currentTimeMillis() - reqTime));
    }
}
