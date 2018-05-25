package up.csd.json.codec;

/**
 * Created by Smile on 2018/5/21.
 */
public class Header {
    public static final String ID = "id";
    public static final String VERSION = "version";
    public static final String LOGID = "log_id";
    public static final String PROVIDER = "provider";
    public static final String MAGICNUM = "magic_num";
    public static final String BODYLEN = "body_len";

    public static final int PROVIDER_LENGTH = 16;
    public static final int LENGTH = 32; // 2+2+4+16+4+4

    private int id = 1;
    private int version = 1;
    private long logId = 1;
    private int reqId1 = 1;
    private int reqId2 = 1;
    private int reqId3 = 1;
    private int reqId4 = 1;
    private String provider = "";
    private long magicNum = -1;
    private long bodyLen = -1;

    public RequestId getReqId() {
        RequestId reqId = new RequestId(reqId1, reqId2, reqId3, reqId4);
        return reqId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public long getLogId() {
        return logId;
    }

    public void setLogId(long logId) {
        this.logId = logId;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public long getMagicNum() {
        return magicNum;
    }

    public void setMagicNum(long magicNum) {
        this.magicNum = magicNum;
    }

    public long getBodyLen() {
        return bodyLen;
    }

    public void setBodyLen(long bodyLen) {
        this.bodyLen = bodyLen;
    }

    public String toString() {
        return "Header [id=" + id + ", version=" + version + ", logId=" + logId + ", reqId=" + reqId1 + "-" + reqId2 + "-" + reqId3 + "-" + reqId4 + ", provider=" + provider + ", magicNum="
                + magicNum + ", bodyLen=" + bodyLen + "]";
    }

    public String getReqIdAsStr() {
        return reqId1 + "" + reqId2 + "" + reqId3;
    }

    public int getReqId1() {
        return reqId1;
    }

    public void setReqId1(int reqId1) {
        this.reqId1 = reqId1;
    }

    public int getReqId2() {
        return reqId2;
    }

    public void setReqId2(int reqId2) {
        this.reqId2 = reqId2;
    }

    public int getReqId3() {
        return reqId3;
    }

    public void setReqId3(int reqId3) {
        this.reqId3 = reqId3;
    }

    public int getReqId4() {
        return reqId4;
    }

    public void setReqId4(int reqId4) {
        this.reqId4 = reqId4;
    }
}
