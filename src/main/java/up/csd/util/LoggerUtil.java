package up.csd.util;

import io.netty.util.CharsetUtil;
import org.apache.log4j.*;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Smile on 2018/5/22.
 */
public class LoggerUtil {

    public static Logger myLogger = Logger.getLogger(LoggerUtil.class);

    public static String LOG_PATTERN = "[%d{yyyy-MM-dd HH:mm:ss.SSS}][%t][%p]%m%n";

    public static Map<String, Logger> loggers = new ConcurrentHashMap<>();
    public static Map<String, Appender> appenders = new ConcurrentHashMap<>();

    static Field fieldHt;
    static Constructor<?> constructorCategoryKey;

    static {
        try {
            fieldHt = Hierarchy.class.getDeclaredField("ht");
            fieldHt.setAccessible(true);

            constructorCategoryKey = Class.forName("org.apache.log4j.CategoryKey").getDeclaredConstructor(String.class);
            constructorCategoryKey.setAccessible(true);

        } catch (NoSuchFieldException | SecurityException | NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
            myLogger.warn("", e);
        }
    }

    public static Logger getLogger(String logFile, String logName) {
        Logger logger = Logger.getLogger(logName);

        logger.removeAllAppenders();
        logger.setLevel(Level.INFO);

        logger.setAdditivity(true); // 是否继承父logger

        RollingFileAppender appender = new RollingFileAppender();
        PatternLayout layout = new PatternLayout();
        layout.setConversionPattern(LOG_PATTERN);
        appender.setLayout(layout);

        appender.setFile(logFile);
        appender.setMaxFileSize("20MB");
        appender.setEncoding(CharsetUtil.UTF_8.name());
        appender.setAppend(true);

        appender.activateOptions();

        logger.addAppender(appender);

        loggers.put(logName, logger);
        appenders.put(logName, appender);

        return logger;
    }

    public static String getLogFileName(Logger logger) {
        return ((FileAppender) logger.getAllAppenders().nextElement()).getFile();
    }

    public static File getLogFile(Logger logger) {
        return new File(getLogFileName(logger));
    }

    public static void removeLogger(Logger logger) {
        removeLogger(logger.getName());
    }

    public static void removeLogger(String logName) {
        appenders.get(logName).close();
        appenders.remove(logName);
        loggers.get(logName).removeAllAppenders();
        Hierarchy hierarchy = (Hierarchy) loggers.get(logName).getLoggerRepository();
        try {
            Hashtable<?, ?> ht = (Hashtable<?, ?>) fieldHt.get(hierarchy);
            Object categoryKey = constructorCategoryKey.newInstance(logName);
            ht.remove(categoryKey);
        } catch (InvocationTargetException | IllegalArgumentException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
            myLogger.warn("", e);
        }
        loggers.remove(logName);
    }
}
