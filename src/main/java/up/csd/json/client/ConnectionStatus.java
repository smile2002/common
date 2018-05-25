package up.csd.json.client;

/**
 * Created by Smile on 2018/5/22.
 */
public enum ConnectionStatus {

    AVAILABLE("0", "链路正常"),
    UNDER_MAINTENANCE("1", "服务正在升级，暂不可用"),
    CONNECTED("2", "已连接,待启用"),
    NOT_AVAILABLE("Z", "链路不可用");

    private String code;
    private String desc;

    private ConnectionStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public String getCode() {
        return code;
    }
}
