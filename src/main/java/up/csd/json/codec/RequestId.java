package up.csd.json.codec;

/**
 * Created by Smile on 2018/5/21.
 */
public class RequestId {
    public int id1 = 0;
    public int id2 = 0;
    public int id3 = 0;
    public int id4 = 0;

    public RequestId(int id1, int id2, int id3, int id4) {
        this.id1 = id1;
        this.id2 = id2;
        this.id3 = id3;
        this.id4 = id4;
    }

    public RequestId(int[] ids) {
        this.id1 = ids[0];
        this.id2 = ids[1];
        this.id3 = ids[2];
//		this.id3 = ids[3];
    }

    public static RequestId newInstance() {
        return new RequestId(ReqIdGenerator.genReqIdArr());
    }

    public String toString() {
        return id1 + "-" + id2 + "-" + id3 + "-" + id4;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id1;
        result = prime * result + id2;
        result = prime * result + id3;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RequestId other = (RequestId) obj;
        if (id1 != other.id1)
            return false;
        if (id2 != other.id2)
            return false;
        if (id3 != other.id3)
            return false;
        return true;
    }
}
