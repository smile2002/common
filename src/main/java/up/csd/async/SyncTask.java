package up.csd.async;

/**
 * Created by Smile on 2018/5/22.
 */
public abstract class SyncTask extends AsyncTask {

    @Override
    public Object id() {
        // sync task does not need async id.
        return null;
    }

    @Override
    public AsyncType type() {
        return AsyncType.NOT_ASYNC;
    }

    @Override
    public String toString() {
        return "Task[" + name() + "]";
    }
}