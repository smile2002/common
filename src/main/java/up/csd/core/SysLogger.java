package up.csd.core;

import org.apache.log4j.Logger;

/**
 * Created by Smile on 2018/5/22.
 */
public class SysLogger {

    public static final Logger logger = Logger.getLogger("sys.log");

    public static void trace(String message) {
        logger.trace(message);
    }
    public static void trace(String message, Throwable e) {
        logger.trace(message, e);
    }

    public static void debug(String message) {
        logger.debug(message);
    }
    public static void debug(String message, Throwable e) {
        logger.debug(message, e);
    }

    public static void info(String message) {
        logger.info(message);
    }
    public static void info(String message, Throwable e) {
        logger.info(message, e);
    }

    public static void warn(String message) {
        logger.warn(message);
    }
    public static void warn(String message, Throwable e) {
        logger.warn(message, e);
    }

    public static void error(String message) {
        logger.error(message);
    }
    public static void error(String message, Throwable e) {
        logger.error(message, e);
    }
}
