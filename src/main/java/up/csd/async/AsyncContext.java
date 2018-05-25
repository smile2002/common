package up.csd.async;

/**
 * Created by Smile on 2018/5/22.
 */

public interface AsyncContext {
    /**
     * 用来记录日志的ID
     */
    public abstract String logId();

    /**
     * 重写该方法用于设置回调方法需要的异步处理结果
     * @param message
     */
    public default <T> void message(T message) {
        // 异步任务需要重写该方法获取上一部返回
    }

    /**
     * 重写该方法获取回调方法需要的上一步处理结果
     */
    public default <T> T message() {
        // 异步任务需要重写该方法获取上一部返回
        return null;
    }
}
