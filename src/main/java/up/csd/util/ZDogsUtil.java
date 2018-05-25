package up.csd.util;

import com.unionpay.common.jlog.JLogger;
import com.unionpay.common.util.PropertyUtil;
import up.csd.core.SysLogger;
import up.csd.core.ZDogsCode;

import java.io.IOException;

/**
 * Created by Smile on 2018/5/22.
 */
public class ZDogsUtil {

    private static org.apache.log4j.Logger log4jLogger = org.apache.log4j.Logger.getLogger(ZDogsUtil.class);
    private static com.unionpay.common.log.Logger zdogsLogger = com.unionpay.common.log.Logger.getLogger(ZDogsUtil.class);

    public static String MODULE_ID = "ALL";

    public static void sendError(ZDogsCode zdogsCode, Object... params) {
        SysLogger.error(zdogsCode.getCode() + "-" + zdogsCode.getDesc());
        zdogsLogger.sendErrUdpMsg(zdogsCode.getErrorCode(), MODULE_ID,
                com.unionpay.common.log.Logger.Level.ERROR, zdogsCode.getDesc(), params);
    }

    public static void init(String path) throws IOException {
        if (!StringUtil.isEmpty(path)) {
            log4jLogger.info("read jlog from : " + path);
        } else {
            log4jLogger.info("read jlog from classPath");
        }
        JLogger.init(PropertyUtil.getInstance().getPropertiesByFileName(path,com.unionpay.common.jlog.Contants.ZDOGS_SERVER_CONFIG_FILE_NAME));
    }
}
