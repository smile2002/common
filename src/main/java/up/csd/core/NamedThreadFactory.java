package up.csd.core;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Smile on 2018/5/21.
 */
public class NamedThreadFactory implements ThreadFactory {
    final ThreadGroup group;
    final AtomicInteger threadNumber = new AtomicInteger(1);
    final String name;
    final boolean deamon;

    public NamedThreadFactory(final String name) {
        this(name, true);
    }

    public NamedThreadFactory(final String name, final boolean deamon) {
        final SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        this.name = name;
        this.deamon = deamon;
    }

    public Thread newThread(final Runnable runnable) {
        final Thread t = new Thread(group, runnable, name + "-thread-" + threadNumber.getAndIncrement());
        t.setDaemon(deamon);
        if (t.getPriority() != Thread.NORM_PRIORITY) {
            t.setPriority(Thread.NORM_PRIORITY);
        }
        return t;
    }
}