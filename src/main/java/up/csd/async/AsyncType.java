package up.csd.async;

/**
 * Created by Smile on 2018/5/22.
 */
public class AsyncType {

    public static final AsyncType NOT_ASYNC = new AsyncType(-1, "表示这种类型的事件其实是个同步事件！！！");
    public static final AsyncType MYCTX = new AsyncType(0, "MYCTX");
    public static final AsyncType JSON_REQ = new AsyncType(1, "使用JSON协议调用服务时使用");
    public static final AsyncType MAGPIE_REQ = new AsyncType(2, "随便写一个，大概表达个意思，真需要用的时候需要根据情况再细化表述");

    int hashCode;
    String desc;

    private AsyncType(int hashCode, String desc) {
        this.desc = desc;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
//		result = prime * result + ((desc == null) ? 0 : desc.hashCode());
        result = prime * result + hashCode;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
//		if (getClass() != obj.getClass())
//			return false;
        AsyncType other = (AsyncType) obj;
        if (desc == null) {
            if (other.desc != null)
                return false;
        } else if (!desc.equals(other.desc))
            return false;
        if (hashCode != other.hashCode)
            return false;
        return true;
    }

    public static void main(String[] args) {
        System.out.println(JSON_REQ.hashCode());
        System.out.println(MAGPIE_REQ.hashCode());
        System.out.println(MYCTX.hashCode());
    }
}