package up.csd.async;

/**
 * Created by Smile on 2018/5/22.
 */

public interface AsyncContext {
    /** 记录全局日志ID **/
    String logId();

    /** 设置(及获取)客户端原始请求 **/
    default <R> void clientRequest(R message) { }
    default <R> R clientRequest() { return null; }

    /** 设置(及获取)回调方法需要的最近一次异步处理结果 **/
    default <T> void serverResponse(T message) { }
    default <T> T serverResponse() { return null; }
}
