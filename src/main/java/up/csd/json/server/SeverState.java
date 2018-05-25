package up.csd.json.server;

/**
 * Created by Smile on 2018/5/22.
 */
public final class SeverState {

    private static final SeverState INSTANCE = new SeverState();

    private String pongType = "0";

    private SeverState() { }

    public static String getPongType() {
        return INSTANCE.pongType;
    }

    /**
     * 系统升级
     */
    public static void upgrade() {
        INSTANCE.pongType = "1";
    }

    /**
     * 系统恢复
     */
    public static void restore() {
        INSTANCE.pongType = "0";
    }
}