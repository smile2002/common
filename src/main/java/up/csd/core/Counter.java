package up.csd.core;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import up.csd.util.MiscUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Smile on 2018/5/22.
 */
public class Counter {

    public AtomicLong CONN_COUNT = new AtomicLong(0);

    public AtomicLong REQ_COUNT = new AtomicLong(0);
    public AtomicLong PENDING_COUNT = new AtomicLong(0);
    public AtomicLong TIMEOUT_COUNT = new AtomicLong(0);
    public AtomicLong TOTAL_TIME_USED = new AtomicLong(0);
    public AtomicLong LAST_REQ_COUNT = new AtomicLong(0);

    public Map<String, AtomicLong> URI_COUNT_MAP = new ConcurrentHashMap<>();
    public ConcurrentLinkedQueue<Double> tpQue = new ConcurrentLinkedQueue<Double>();

    public long TPS = 0;
    public long AVG = 0;

    public long COUNT_PERIOD = 3;
    public Double tp = 95.0;

    private AtomicBoolean started = new AtomicBoolean(false);

    private String name;
    public Counter(String name) {
        this.name = name;
    }

    public void clear() {
        if (!started.get()) {
            return;
        }
        LAST_REQ_COUNT.set(0);
        TOTAL_TIME_USED.set(0);
        REQ_COUNT.set(0);
        TIMEOUT_COUNT.set(0);
        URI_COUNT_MAP.clear();
        TPS = 0;
        AVG = 0;
        tpQue = new ConcurrentLinkedQueue<Double>();
    }

    public void incConn() {
        if (started.get()) {
            CONN_COUNT.incrementAndGet();
        }
    }

    public void decConn() {
        if (started.get()) {
            CONN_COUNT.decrementAndGet();
        }
    }

    public void incReq() {
        if (started.get()) {
            PENDING_COUNT.incrementAndGet();
        }
    }

    private void decPending() {
        if (started.get()) {
            PENDING_COUNT.decrementAndGet();
            REQ_COUNT.incrementAndGet();
        }
    }

    public void addTime(long time) {
        if (started.get()) {
            decPending();
            TOTAL_TIME_USED.addAndGet(time);
            tpQue.offer((double) time);
        }
    }

    public void incTimeout(long time) {
        if (started.get()) {
            TIMEOUT_COUNT.incrementAndGet();
        }
    }

    // TODO 该方法存在内存溢出风险
    public void incReq(String uri) {
        if (!started.get()) {
            return;
        }
        if (uri != null) {
            AtomicLong reqCounter = URI_COUNT_MAP.get(uri);
            if (reqCounter == null) {
                synchronized (URI_COUNT_MAP) {
                    reqCounter = URI_COUNT_MAP.get(uri);
                    if (reqCounter == null) {
                        reqCounter = new AtomicLong(0);
                        URI_COUNT_MAP.put(uri, new AtomicLong(0));
                    }
                }
            }
            reqCounter.incrementAndGet();
        }
        incReq();
    }

    public String printUriCountMap() {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, AtomicLong> entry : URI_COUNT_MAP.entrySet()) {
            builder.append("\n");
            builder.append(entry.getKey() + " -> " + entry.getValue());
        }
        return builder.toString();
    }

    public Counter start() {
        if (started.getAndSet(true)) {
            return this;
        }
        new Thread(() -> {
            while (true) {
                try {
                    TimeUnit.SECONDS.sleep(COUNT_PERIOD);
                    long reqCount = REQ_COUNT.get();
                    long reqAmountInPeriod = reqCount - LAST_REQ_COUNT.get();
                    long timeUsedInPeriod = TOTAL_TIME_USED.get();
                    LAST_REQ_COUNT.set(reqCount);
                    TOTAL_TIME_USED.set(0);
                    if (reqAmountInPeriod == 0) {
                        AVG = 0;
                    } else {
                        AVG = timeUsedInPeriod / reqAmountInPeriod;
                    }
                    TPS = reqAmountInPeriod / COUNT_PERIOD;
                    SysLogger.info("Counter[" + name + "]"
                            + " conn=" + CONN_COUNT.get()
                            + ", req=" + reqCount
                            + ", pending=" + PENDING_COUNT.get()
                            + ", timeout=" + TIMEOUT_COUNT.get()
                            + ", tps=" + TPS
                            + ", avg=" + AVG
                            + ", tp" + tp.intValue() + "=" + getTp() );
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "counter-thread-" + name).start();

        return this;
    }

    private int getTp() {
        Double[] array = tpQue.toArray(new Double[0]);
        tpQue = new ConcurrentLinkedQueue<Double>();
        double[] darray = new double[array.length];
        copyDoubleArray(array, darray);
        Percentile p = new Percentile();
        p.setData(darray);
        Double ret = p.evaluate(tp);
        return new Double(ret).intValue();
    }

    private void copyDoubleArray(Double[] srcArray, double[] ddouble) {
        for (int i = 0; i < srcArray.length; i++) {
            ddouble[i] = srcArray[i];
        }
    }

    public static void main(String[] args) {
        Counter c = new Counter("Test");
        c.start();

        new Thread(() -> {
            while (true) {
                for (int i = 0; i < RandomUtils.nextInt(10); i++) c.incConn();
                for (int i = 0; i < RandomUtils.nextInt(9); i++) c.decConn();
                for (int i = 0; i < RandomUtils.nextInt(1000); i++) c.incReq();
                for (int i = 0; i < RandomUtils.nextInt(5); i++) c.incTimeout(500);
                for (int i = 0; i < RandomUtils.nextInt(1000); i++) c.addTime(RandomUtils.nextInt(100));
                MiscUtil.waitFor(10);
            }
        }).start();
    }
}