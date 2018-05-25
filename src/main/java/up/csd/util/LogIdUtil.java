package up.csd.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Smile on 2018/5/22.
 */
public class LogIdUtil {

    protected static final Logger logger = Logger.getLogger(LogIdUtil.class);

    private static String machineIp;

    private static final transient AtomicInteger ID = new AtomicInteger(new Random().nextInt(99999999));
    private static final DateFormat df = new SimpleDateFormat("yyMMddHHmmss");

    public static final String SYS_ID_CORE = "AT00";
    public static final String SYS_ID_RCV = "AT01";
    public static final String SYS_ID_BIZ = "AT02";
    public static final String SYS_ID_SND = "AT03";

    static {
        try {
            String[] ips = InetAddress.getLocalHost().getHostAddress().split("\\.");
            machineIp = StringUtils.leftPad(Integer.toHexString(Integer.parseInt(ips[2])), 2, '0')
                    + StringUtils.leftPad(Integer.toHexString(Integer.parseInt(ips[3])), 2, '0');
        } catch (Exception e) {
            throw new RuntimeException("Init machineIp failed", e);
        }
    }

    public static String genAndSetMdc(String systemId) {
        String id = genId(systemId);
        MDC.put("LOGID", id);
        return id;
    }

    public static String getFromMdc() {
        return (String) MDC.get("LOGID");
    }

    public static void setMdc(String logId) {
        Validate.notNull(StringUtils.trimToNull(logId), "logId is blank!");
        MDC.put("LOGID", logId);
    }

    public static void removeMdc() {
        MDC.remove("LOGID");
    }

    public static void setMdcAndRemoveLogId(Map<String, String> map) {
        String logId = map.remove("__log_id__");
        setMdc(logId);
    }

    /**
     * 根据系统ID、日期、IP地址、自增数生成LOGID
     * @param systemId 四位系统ID
     * @return
     */
    public static String genId(String systemId) {
        if (StringUtils.isBlank(systemId) || systemId.length() != 4) {
            throw new IllegalArgumentException("systemId=" + systemId);
        }
        if (ID.get() > 900000000) {
            synchronized(LogIdUtil.class) {
                if (ID.get() > 900000000) {
                    ID.set(1);
                }
            }
        }
        return systemId
                + df.format(new Date())
                + machineIp
                + StringUtils.leftPad(String.valueOf(ID.incrementAndGet()), 10, '0');
    }

    public static void main(String[] args) {
        for (int i = 0; i < 1000000; i ++) {
            System.out.println(genId(SYS_ID_RCV));
        }
        Date now = new Date();
        System.out.println(df.format(now));
    }

}