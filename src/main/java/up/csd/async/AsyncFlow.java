package up.csd.async;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import up.csd.core.NamedThreadFactory;
import up.csd.core.SysLogger;
import up.csd.util.LoggerUtil;
import up.csd.util.MiscUtil;
import up.csd.util.SysConfUtil;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by Smile on 2018/5/22.
 */
public class AsyncFlow {

    public static final ConcurrentHashMap<AsyncType, ConcurrentHashMap<Object, AsyncTask>> UNIVERSAL_CONTEXT = new ConcurrentHashMap<>();
    protected static final ExecutorService internalDefault = Executors.newFixedThreadPool(10, new NamedThreadFactory("async-flow-default"));
    private static final Logger statusLogger = LoggerUtil.getLogger("logs/asyncflow.log", AsyncFlow.class.getSimpleName() + ".status.logger");
    public static final ConcurrentSkipListMap<Long, ConcurrentLinkedQueue<AsyncTask>> timeoutCtrlMap = new ConcurrentSkipListMap<>();

    private volatile int defaultTimeout = 15;
    private volatile TimeUnit defaultTimeoutTimeUnit = TimeUnit.SECONDS;

    private AsyncFuture future;
    protected volatile boolean terminated = false;
    private volatile int curr = -1; // -1 means not started yet
    private List<AsyncTask> tasks = new ArrayList<>(); // serial
    private List<AsyncTask> ptasks = new ArrayList<>(); // parallel
    protected ExecutorService defaultBy;

    public void future(AsyncFuture future) {
        this.future = future;
    }
    public AsyncFuture future() {
        return this.future;
    }


    static {

        // TODO 将statusLogger改成异步的
//		statusLogger.setLevel(Level.DEBUG);
        Executors.newFixedThreadPool(1, new NamedThreadFactory("asyncflow-status-logger")).execute(() -> {
            while (true) {
                try {
                    StringBuilder sb = new StringBuilder();
                    sb.append("NOT_ASYNC=" + sizeOf(UNIVERSAL_CONTEXT.get(AsyncType.NOT_ASYNC)));
                    sb.append(", MYCTX=" + sizeOf(UNIVERSAL_CONTEXT.get(AsyncType.MYCTX)));
                    sb.append(", UPHEAD_REQ=" + sizeOf(UNIVERSAL_CONTEXT.get(AsyncType.JSON_REQ)));
                    sb.append(", MAGPIE_REQ=" + sizeOf(UNIVERSAL_CONTEXT.get(AsyncType.MAGPIE_REQ)));
                    printCurrentTasks(UNIVERSAL_CONTEXT.get(AsyncType.JSON_REQ), sb);
                    statusLogger.info(sb.toString());
                    MiscUtil.waitFor(2000);
                } catch (Exception e) {
                    statusLogger.warn("asyncflow-timeout-exception", e);
                }
            }
        });
        Executors.newFixedThreadPool(1, new NamedThreadFactory("asyncflow-timeout")).execute(() -> {
            while (true) {
                try {
                    if (timeoutCtrlMap.isEmpty()) {
                        MiscUtil.waitFor(100);
                        continue;
                    }
                    long firstTimeout = timeoutCtrlMap.firstKey();
                    if (firstTimeout >= System.currentTimeMillis()) {
                        MiscUtil.waitFor(100);
                        continue;
                    }
                    Map.Entry<Long, ConcurrentLinkedQueue<AsyncTask>> entry = timeoutCtrlMap.pollFirstEntry();
                    if (entry == null) {
                        continue;
                    }
                    if (entry.getKey() < System.currentTimeMillis()) {
                        ConcurrentLinkedQueue<AsyncTask> q = entry.getValue();
                        AsyncTask task = q.poll();
                        while (task != null) {
                            forgetTask(task);
                            task.doFailTimeout();
                            task = q.poll();
                        }
                    }
                } catch (NoSuchElementException e) {
                    // removed by other thread, just ignore
                } catch (Exception e) {
                    statusLogger.warn("asyncflow-timeout-thread exception", e);
                }
            }
        });
//		Executors.newFixedThreadPool(1, new NamedThreadFactory("asyncflow-timeout-patch")).execute(() -> {
//			while (true) {
//				try {
//					Util.waitFor(10 * 1000);
//					Iterator<Entry<AsyncType, ConcurrentHashMap<Object, AsyncTask>>> o = UNIVERSAL_CONTEXT.entrySet().iterator();
//					while (o.hasNext()) {
//						Entry<AsyncType, ConcurrentHashMap<Object, AsyncTask>> oo = o.next();
//						Iterator<Entry<Object, AsyncTask>> i = oo.getValue().entrySet().iterator();
//						while (i.hasNext()) {
//							Entry<Object, AsyncTask> ii = i.next();
//							AsyncTask task = ii.getValue();
//							if (task.isTimeout()) {
//								i.remove();
//								task.doFailTimeout();
//							}
//						}
//					}
//				} catch (Exception e) {
//					statusLogger.warn("asyncflow-timeout-patch-thread exception", e);
//				}
//			}
//		});
    }


    /**
     * 指定第一个任务
     * @param doSomething 一个异步任务
     */
    public static AsyncFlow first(AsyncTask doSomething) {
        Validate.notNull(doSomething, "Task cannot be null!");
        AsyncFlow flow = new AsyncFlow();
        if(doSomething != null) {
            flow.addTask(doSomething);
        }
        return flow;
    }
    public static AsyncFlow first(AsyncTask[] tasks) {
        Validate.notNull(tasks, "Tasks cannot be null!");
        AsyncFlow flow = new AsyncFlow();
        flow.then(tasks);
        return flow;
    }
    public static AsyncFlow first(List<AsyncTask> tasks) {
        Validate.notNull(tasks, "Tasks cannot be null!");
        AsyncFlow flow = new AsyncFlow();
        flow.then(tasks);
        return flow;
    }

    /**
     * TODO 构造一个动态流程，task执行过程中动态添加后续task或flow
     * @param doSomething
     * @return
     */
    public static AsyncFlow dynamic(AsyncTask doSomething) {
        // TODO
        return null;
    }

    /**
     * TODO 嵌套子流程
     * @param processSomeFlow
     * @return
     */
    public AsyncFlow flow(AsyncFlow processSomeFlow) {
        // TODO
        return this;
    }

    /**
     * 指定下一个任务
     * @param doSomething 一个异步任务
     * @return
     */
    public AsyncFlow then(AsyncTask doSomething) {
        Validate.notNull(doSomething);
        this.addTask(doSomething);
        return this;
    }

    /**
     * 连续指定后续几个任务
     * @param tasks
     * @return
     */
    public AsyncFlow then(AsyncTask[] tasks) {
        for (AsyncTask task : tasks) {
            then(task);
        }
        return this;
    }
    public AsyncFlow then(List<AsyncTask> tasks) {
        for (AsyncTask task : tasks) {
            then(task);
        }
        return this;
    }

    public AsyncFlow parallel(AsyncTask doSomething) {
        this.addParallel(doSomething);
        return this;
    }

    /**
     * 就执行已经指定的这些任务吧
     */
    public AsyncFuture start() {
        internalDefault.execute(() -> {
            nextTask();
            pf_ck();
        });
        return new AsyncFuture(this);
    }

    /**
     * 该方法将抛出一个业务异常[TaskFlowQuitException]从而终止整个flow的执行。
     */
    public void terminate() {
        this.terminated = true;
        forgetTask(tasks.get(curr));
        notifyFuture();
        throw new TaskFlowQuitException();
    }

    /**
     * 指定前一个任务的回调方法使用的线程执行者（一般是一个线程池）<br>
     * 该方法不是必须的<br>
     * 如果不指定，将使用AsyncFlow内置的默认线程池。<br>
     * @param executor 线程执行者
     * @return
     */
    public AsyncFlow by(ExecutorService executor) {
        Validate.notNull(executor, "executor should not be null");
        latestTask().by(executor);
        return this;
    }

    /**
     * 指定该AsyncFlow给所有任务的回调方法默认使用的线程执行者（一般是一个线程池）<br>
     * 该方法不是必须的<br>
     * 如果不指定，该AsyncFlow将使用其内置的不限制大小的默认线程池。<br>
     * @param executor 线程执行者
     * @return 调用者自己
     */
    public AsyncFlow defaultBy(ExecutorService executor) {
        defaultBy = executor;
        return this;
    }

    /**
     * 指定前一个任务在异步阶段的超时时间
     * @param timeout 必须大于0才有效
     * @param timeUnit 不能为null
     */
    public AsyncFlow timeout(int timeout, TimeUnit timeUnit) {
        if (timeout > 0) {
            Validate.notNull(timeUnit);
            latestTask().timeout(timeout, timeUnit);
        }
        return this;
    }
    /**
     * 指定前一个任务在异步阶段的超时时间，单位：秒
     * @param timeout 必须大于0才有效
     */
    public AsyncFlow timeout(int timeout) {
        return timeout(timeout, TimeUnit.SECONDS);
    }

    /**
     * 指定该AsyncFlow默认的任务在异步阶段的超时时间
     * @param timeout 必须大于0才有效
     * @param timeUnit 不能为null
     * @return
     */
    public AsyncFlow defaultTimeout(int timeout, TimeUnit timeUnit) {
        Validate.isTrue(timeout > 0);
        Validate.notNull(timeUnit);
        defaultTimeout = timeout;
        defaultTimeoutTimeUnit = timeUnit;
        return this;
    }

    /**
     * 唤醒之前挂起的任务
     * @param asyncType
     * @param asyncId
     * @param resp
     * @return 根据contextType和contextId找到的flow，如果没找到则返回null
     */
    public static <T> void wakeup(AsyncType asyncType, Object asyncId, T message) {
        AsyncTask task = forgetTask(asyncType, asyncId);
        if (task == null) {
            SysLogger.warn("AsyncTask of type[" + asyncType + "] is not found by id[" + asyncId + "]");
            return;
        }
        task.getExecutor().submit(() -> {
            synchronized (task.flow()) {

                if (task.flow().terminated) {
                    SysLogger.warn(task.nameWithType() + "U r late, the flow is already terminated.");
                    return;
                }

                task.context().serverResponse(message);
                task.doCallback();
            }
        });
    }

    private static final int maxAsyncTasks = Integer.parseInt(StringUtils.defaultString(
            StringUtils.trimToNull(SysConfUtil.get("asyncflow.max.tasks")), "10000"));
    public static void rememberTask(AsyncTask task) {
        ConcurrentHashMap<Object, AsyncTask> map = getOrCreateContextMap(task.type());
        int count = map.size();
        if (count > maxAsyncTasks) {
            SysLogger.warn("Too many AsyncTasks is running: " + count);
            throw new TaskExceedException();
        }
        setTimeout(task);
        SysLogger.info("remember task id = " + task.id());
        map.putIfAbsent(task.id(), task);
    }


    private static void setTimeout(AsyncTask task) {
        if (task.timeout == 0) {
            task.timeout = task.flow().defaultTimeout;
            task.timeoutTimeUnit = task.flow().defaultTimeoutTimeUnit;
        }
        task.timeoutTime = System.currentTimeMillis() + task.timeoutTimeUnit.toMillis(task.timeout);
        ConcurrentLinkedQueue<AsyncTask> q = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<AsyncTask> preQ = timeoutCtrlMap.putIfAbsent(task.timeoutTime, q);
        if (preQ != null) {
            q = preQ;
        }
        synchronized (q) {
            q.add(task);
            timeoutCtrlMap.putIfAbsent(task.timeoutTime, q);
        }
    }
    private static void cancelTimeout(AsyncTask task) {
        ConcurrentLinkedQueue<AsyncTask> q = timeoutCtrlMap.get(task.timeoutTime);
        if (q != null) {
            q.remove(task);
            if (q.isEmpty()) {
                synchronized (q) {
                    if (q.isEmpty()) {
                        timeoutCtrlMap.remove(task.timeoutTime);
                    }
                }
            }
        }
    }

    public static AsyncTask forgetTask(AsyncTask task) {
        cancelTimeout(task);
        ConcurrentHashMap<Object, AsyncTask> contextMap = UNIVERSAL_CONTEXT.get(task.type());
        if (contextMap != null) {
            contextMap.remove(task.id());
        }
        return task;
    }

    public static AsyncTask forgetTask(AsyncType asyncType, Object asyncId) {
        ConcurrentHashMap<Object, AsyncTask> contextMap = UNIVERSAL_CONTEXT.get(asyncType);
        AsyncTask task = (contextMap == null ? null : (AsyncTask) contextMap.remove(asyncId));
        if (task != null) {
            cancelTimeout(task);
        }
        return task;
    }

    private void addTask(AsyncTask wtf) {
        wtf.flow(this);
        tasks.add(wtf);
    }

    private void addParallel(AsyncTask wtf) {
        ptasks.add(wtf);
    }

    private void pf_ck() {
        for (AsyncTask task : ptasks) {
            SysLogger.warn("Paralle task is not supported yet. Task name: " + task.name());
            // TODO To be implemented
        }
    }

    protected void nextTask() {
        ++curr;
        if (checkToFinish()) {
            return;
        }
        AsyncTask task = tasks.get(curr);
        if (task.isFinished()) {
            String err = "The status[" + task.status + "] of the current task is illegal!!!";
            SysLogger.error(err);
            task.failDoo(new IllegalStateException(err));
            return;
        }
        if (curr != 0) {
            task.context(tasks.get(0).context());
        }

        task.doDoo();
    }

    public final <ContextType extends AsyncContext> ContextType context() {
        return tasks.get(0).context();
    }

    public final <ContextType extends AsyncContext> AsyncFlow context(ContextType context) {
        Validate.isTrue(tasks.size() > 0);
        tasks.get(0).context(context);
        return this;
    }

    private static ConcurrentHashMap<Object, AsyncTask> getOrCreateContextMap(AsyncType asyncType) {
        ConcurrentHashMap<Object, AsyncTask> map = UNIVERSAL_CONTEXT.get(asyncType);
        if (map == null) {
            map = new ConcurrentHashMap<>();
            ConcurrentHashMap<Object, AsyncTask> qam = UNIVERSAL_CONTEXT.putIfAbsent(asyncType, map);
            if (qam != null) {
                map = qam;
            }
        }
        return map;
    }

    private AsyncTask latestTask() {
        int size = tasks.size();
        if (size == 0) {
            throw new RuntimeException("You should add at least one " + AsyncTask.class.getName()
                    + " to call AsyncFlow.by(ExecutorService executor)");
        }
        return tasks.get(size - 1);
    }
    protected boolean checkToFinish() {
        if (terminated || curr == tasks.size()) {
            if (tasks.get(curr - 1).isFinished()) {
                notifyFuture();
                return true;
            }
        }
        return false;
    }
    public void notifyFuture() {
        if (future != null) {
            synchronized(future) {
                future.notify();
            }
        }
    }

    private static int sizeOf(ConcurrentHashMap<Object, AsyncTask> map) {
        return map == null ? 0 : map.size();
    }
    private static void printCurrentTasks(ConcurrentHashMap<Object, AsyncTask> map, StringBuilder sb) {
        if (map == null || map.size() == 0) {
            return;
        }
        sb.append(", Current Flow Tasks:");
        int size = map.size();
        for (AsyncTask task : map.values()) {
            try {
                sb.append("\ntask: " + task.nameWithType());
                sb.append(", timeout ");
                sb.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(task.timeoutTime)));
            } catch (IndexOutOfBoundsException e) {
                // 交易过程中会动态变化，越界直接忽略即可
            }
        }
        sb.append("\nmap size: " + size);
    }
}
