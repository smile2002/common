package up.csd.async;

/**
 * Created by Smile on 2018/5/22.
 */
public class AsyncFuture {

    public AsyncFuture(AsyncFlow flow) {
        flow.future(this);
    }

    public void sync() throws InterruptedException {
        synchronized (this) {
            boolean interrupted = false;
            try {
                wait();
            } catch (InterruptedException e) {
                interrupted = true;
            }
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
