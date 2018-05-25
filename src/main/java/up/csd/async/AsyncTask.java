package up.csd.async;

import org.apache.log4j.Logger;
import up.csd.util.LogIdUtil;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Smile on 2018/5/22.
 */
public abstract class AsyncTask {

    protected static Logger logger = Logger.getLogger(AsyncTask.class);
    private static final String DEFAULT_NAME =  "default-task-name";

    public long createTime = System.currentTimeMillis();
    public volatile long timeoutTime;
    protected volatile int timeout = 0;
    protected volatile TimeUnit timeoutTimeUnit = TimeUnit.SECONDS;
    private String name = DEFAULT_NAME;
    private String nameWithType = getClass().getSimpleName() + "[" + name + "]";

    public boolean isSync;
    protected volatile TaskStatus status = TaskStatus.INIT;

    public ExecutorService by;
    private Object context;
    private AsyncFlow flow;


    /**
     * 如果该AsyncTask类没有实现callback方法则被认为是一个同步任务。
     */
    {
        try {
            Method method = this.getClass().getMethod("callback");
            isSync = method.getDeclaringClass() != this.getClass();
        } catch (NoSuchMethodException | SecurityException e) {
            isSync = true;
        }
    }

    /**
     * 返回异步事件的类型
     */
    public abstract AsyncType type();

    /**
     * 返回 异步事件的ID
     */
    public abstract Object id();

    /**
     * 该事件的起始业务逻辑，子类需要实现该方法。<br>
     * 注意：在doo()中真正调用异步处理过程前，需要先使用rememberMe()暂存该task，以便异步处理结束后继续执行。
     * @throws Exception
     */
    public abstract void doo() throws Exception;

    protected final boolean isHalfDone() {
        return status == TaskStatus.HALF_DONE;
    }


    public final AsyncFlow flow() {
        return this.flow;
    }
    public final void flow(AsyncFlow flow) {
        this.flow = flow;
    }
    public String name() {
        return name;
    }
    public String nameWithType() {
        return nameWithType;
    }

    public AsyncTask named(String name) {
        this.name = name;
        this.nameWithType = getClass().getSimpleName() + "[" + name + "]";
        return this;
    }

    public final AsyncTask context(Object context) {
        this.context = context;
        return this;
    }
    @SuppressWarnings("unchecked")
    public final <T extends AsyncContext> T context() {
        return (T) context;
    }

    /**
     * 设定异步阶段超时时间，timeout单位：秒
     */
    public AsyncTask timeout(int timeout) {
        return timeout(timeout, TimeUnit.SECONDS);
    }
    /**
     * 设定异步阶段超时时间
     */
    public AsyncTask timeout(int timeout, TimeUnit timeUnit) {
        this.timeout = timeout;
        this.timeoutTimeUnit = timeUnit;
        return this;
    }

    /**
     * 设定异步阶段任务的执行线程池，如果不设定将使用AsyncFlow的默认值
     */
    public AsyncTask by(ExecutorService by) {
        this.by = by;
        return this;
    }
    public ExecutorService getExecutor() {
        if (this.by != null) {
            return this.by;
        } else if (flow.defaultBy != null) {
            return flow.defaultBy;
        } else {
            return AsyncFlow.internalDefault;
        }
    }

    /**
     * 调用callback之前先调用该方法。
     * @throws Exception
     */
    public void beforeCallback() throws Exception {
        // 子类可重写该方法
    }
    /**
     * 该事件的回调业务逻辑，子类可以重写该方法。<br>
     * 注意：在doo()中真正调用异步处理过程前，需要先使用AsyncFlow.rememberTask(this)暂存该task，以便异步处理结束后使用。
     * @throws Exception
     */
    public void callback() throws Exception {
        // 子类可重写该方法
    }

    /**
     * 执行 {@link #doo()} 过程中出现异常，将执行该方法。<br>
     * 子类可重写该方法。
     * @param cause
     */
    public void failDoo(Throwable cause) {
        logger.warn(nameWithType() + " failed in doo()!", cause);
        // do something to repair the task.

        logger.info(nameWithType() + " terminate the whole flow now.");
        this.flow.terminate();
    }
    /**
     * 执行 {@link #doo()} 中的异步过程时出现异常，将执行该方法。<br>
     * 子类可重写该方法。
     * @param cause
     */
    public void failAsync(Throwable cause) {
        logger.warn(nameWithType() + " failed in async step!", cause);
        // do something to repair the task.

        logger.info(nameWithType() + " terminate the whole flow now.");
        this.flow.terminate();
    }
    /**
     * 执行 {@link #callback()} 过程中出现异常，将执行该方法。<br>
     * 子类可重写该方法。
     * @param cause
     */
    public void failCallback(Throwable cause) {
        logger.warn(nameWithType() + " failed in callback()!", cause);
        // do something to repair the task.

        logger.info(nameWithType() + " terminate the whole flow now.");
        this.flow.terminate();
    }
    /**
     * 执行 {@link #doo()} 方法超时后的业务逻辑，子类可以重写该方法。
     */
    public void failTimeout() {
        logger.warn(nameWithType() + " failed because of timeout.");
        // do something to repair the task.


        logger.info(nameWithType() + " terminate the whole flow now.");
        this.flow.terminate();
    }

    /**
     * 异步调用时，将此Task暂存，以便异步处理结束后继续执行。
     */
    public final void rememberMe() {
        AsyncFlow.rememberTask(this);
    }
    public final void forgetMe() {
        AsyncFlow.forgetTask(this);
    }


    /**
     * 就此立刻结束该task。<br>
     * 结束该task并不会影响flow中其他task的执行。
     */
    public final void done() {
        this.status = TaskStatus.DONE;
        throw new TaskIsDoneException();
    }

    public final boolean isTimeout() {
        if (timeoutTime == 0) {
            logger.warn("timeout==0?");
        }
        return timeoutTime < System.currentTimeMillis();
    }


    public final boolean isFinished() {
        if (flow.terminated) {
            logger.warn(nameWithType + " The flow is already terminated.");
            return true;
        }
        if (status == TaskStatus.DONE || status == TaskStatus.FAILED) {
            if (logger.isDebugEnabled()) {
                logger.debug(nameWithType + " The task is already finished.");
            }
            return true;
        }
        return false;
    }

    public final void doDoo() {
        if (isFinished()) {
            return;
        }
        synchronized (flow) {
            try {
                wrapFunc(this::doo, "doo", this::failDoo, "failDoo");
            } finally {
                if (!isFinished()) {
                    if (isSync) {
                        this.status = TaskStatus.DONE;
                    } else {
                        this.status = TaskStatus.HALF_DONE;
                        LogIdUtil.removeMdc();
                        return; // 异步第一阶段就此结束，等待被唤醒
                    }
                }
                flow.nextTask();
                LogIdUtil.removeMdc();
            }
        }
    }
    public final void doCallback() {
        if (isFinished()) {
            return;
        }
        try {
            wrapFunc(this::beforeCallback, "beforeCallback", this::failCallback, "failCallback");
            wrapFunc(this::callback, "callback", this::failCallback, "failCallback");
        } finally {
            if (!isFinished()) {
                this.status = TaskStatus.DONE;
            }
            flow.nextTask();
            LogIdUtil.removeMdc();
        }
    }
    public final void doFailTimeout() {
        if (isFinished()) {
            return;
        }
        try {
            wrapFunc(this::failTimeout, "failTimeout");
        } finally {
            if (this.status == TaskStatus.DONE) {
                flow.nextTask();
            } else if (isFinished()) {
                flow.checkToFinish();
            }
            LogIdUtil.removeMdc();
        }
    }
    public final void doFailAsync(Throwable cause) {
        if (isFinished()) {
            return;
        }
        try {
            wrapFail(this::failAsync, "failAsync", cause);
        } finally {
            if (this.status == TaskStatus.DONE) {
                flow.nextTask();
            } else if (isFinished()) {
                flow.checkToFinish();
            }
            LogIdUtil.removeMdc();
        }
    }

    interface Func {
        void func() throws Exception;
    }
    interface Fail {
        void fail(Throwable cause);
    }
    public final void wrapFunc(Func func, String funcName) {
        wrapFunc(func, funcName, null, null);
    }
    public final void wrapFunc(Func func, String funcName, Fail fail, String failName) {
        try {
            LogIdUtil.setMdc(context().logId());
            if (logger.isDebugEnabled()) {
                logger.debug(nameWithType() + "." + funcName);
            }
            func.func();
        } catch (TaskIsDoneException taskIsDone) {
            if (logger.isDebugEnabled()) {
                logger.debug(nameWithType() + " is done by " + funcName);
            }
        } catch (TaskFlowQuitException terminated) {
            if (logger.isDebugEnabled()) {
                logger.debug("The flow is terminated by " + funcName);
            }
        } catch (Throwable cause) {
            if (fail == null) {
                logger.warn("exception caught when calling " + nameWithType() + "." + funcName + ": " + cause, cause);
                flow.terminate();
            } else {
                wrapFail(fail, failName, cause);
            }
        }
    }
    public final void wrapFail(Fail fail, String failName, Throwable cause) {
        try {
            this.status = TaskStatus.FAILED;
            LogIdUtil.setMdc(context().logId());
            if (logger.isDebugEnabled()) {
                logger.debug(nameWithType() + "." + failName);
            }
            fail.fail(cause);
        } catch (TaskIsDoneException taskIsDone) {
            if (logger.isDebugEnabled()) {
                logger.debug(nameWithType() + " is done by " + failName);
            }
        } catch (TaskFlowQuitException terminated) {
            if (logger.isDebugEnabled()) {
                logger.debug("The flow is terminated by " + failName);
            }
        } catch (Throwable e) {
            logger.warn("exception caught when calling " + nameWithType() + "." + failName + ": " + e, e);
            flow.terminate();
        }
    }

    @Override
    public int hashCode() {
        return this.id().hashCode();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null) {
            return false;
        }
        return this.id().equals(((AsyncTask) that).id());
    }

    @Override
    public String toString() {
        return (isSync ? "S" : "As") + "yncTask[" + name() + "]";
    }
}
