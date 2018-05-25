package up.csd.json.codec;

/**
 * Created by Smile on 2018/5/21.
 */
public class ReqIdGenerator {

    private static int MIN_REQ_ID_SEQ = 9;
    private static int MAX_REQ_ID_SEQ = 999999999;

    private static int reqIdSeq = MIN_REQ_ID_SEQ;

    /**
     * 连续生成三个整数，用于构造Uphead的请求ID
     * @return
     */
    public static int[] genReqIdArr() {
        // TODO reqId第4个int写死，不能随机生成一个，可能和心跳的重复！！！
        int[] arr = new int[4];
        synchronized (ReqIdGenerator.class) {
            for (int i = 0; i < arr.length; i ++) {
                // 如果请求ID超过设置的范围，则抛异常
                if (reqIdSeq > MAX_REQ_ID_SEQ || reqIdSeq < MIN_REQ_ID_SEQ) {
                    int reqIdTmp = reqIdSeq;
                    reqIdSeq = MIN_REQ_ID_SEQ;
                    throw new RuntimeException("Gen ReqId Failed:" + reqIdTmp + "!!!");
                }
                // 如果达到最大值，则将reqIdSeq设置为最小值
                if (reqIdSeq == MAX_REQ_ID_SEQ) {
                    reqIdSeq = MIN_REQ_ID_SEQ;
                }
                arr[i] = reqIdSeq ++;
            }
        }
        return arr;
    }
}
