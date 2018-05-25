package up.csd.util;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import java.io.*;
import java.util.HashMap;
import java.util.Properties;

/**
 * Created by Smile on 2018/5/22.
 */
public class SysConfUtil {

    private static final Logger logger = Logger.getLogger(SysConfUtil.class);

    private static final String SYS_PROP_FILE_NAME = "sys.conf";
    public static String FILE_PATH = null; // 优先读取外部配置文件
    private static SysConfUtil INSTANCE;

    private Properties props;

    /**
     * 根据key获取配置信息<br>
     * 如果未找到，则返回null
     * @param key
     * @return
     */
    public static String get(String key) {
        if (key == null || key.trim().length() == 0) {
            throw new RuntimeException("key can't be empty.");
        }
        if (INSTANCE == null) {
            init();
        }
        return INSTANCE.getProperty(key);
    }

    /**
     * 判断是否是T系统
     * @return
     */
    public static boolean isT() {
        String atConf = get("AT");
        if (!StringUtil.isBlank(atConf)) {
            // 使用contains，支持配置AT=AT
            return atConf.contains("T");
        }
        return false;
    }

    /**
     * 判断是否是A系统
     * @return
     */
    public static boolean isA() {
        String atConf = get("AT");
        if (!StringUtil.isBlank(atConf)) {
            // 使用contains，支持配置AT=AT
            return atConf.contains("A");
        }
        return false;
    }

    public static boolean ignoreRst() {
        return "true".equals(get("ignoreSignRst"));
    }

    /**
     * 延迟实例化单例，需要的时候才载入
     *
     * @return
     */
    public static synchronized void init() {
        if (INSTANCE == null) {
            INSTANCE = new SysConfUtil();
        }
    }

    public static void config(String confDir) {
        System.out.println("[" + StringUtil.getCurrentTime() + "] use conf dir: " + confDir);

        System.setProperty("conf.root.dir", confDir);

        DOMConfigurator.configure(confDir + File.separator + "log4j.x");

        FILE_PATH = confDir + File.separator + SYS_PROP_FILE_NAME;
        init();

        // 初始化zdogs，失败不影响启动
        try{
            ZDogsUtil.init(confDir);
        } catch(Exception ex) {
            logger.error("init zdogs failed", ex);
        }
    }

    /**
     * 不允许自行实例化
     */
    private SysConfUtil() {
        props = new Properties();
        InputStream in = null;

        try {
            if (FILE_PATH != null) {
                logger.info("read config from : " + FILE_PATH);
                in = new FileInputStream(FILE_PATH);
            } else {
                logger.info("read config from classPath");
                in = SysConfUtil.class.getClassLoader().getResourceAsStream(SYS_PROP_FILE_NAME);
            }
            props.load(new InputStreamReader(in, "UTF-8"));

            // 从类路径载入sys.properties配置文件
            // 打印载入的配置信息
            if (!props.isEmpty()) {
                StringBuilder sb = new StringBuilder("\nsys.conf size is " + props.size() + ", content {\n");
                for (Object key : props.keySet()) {
                    sb.append("\t" + key + "=" + props.getProperty((String) key) + "\n");
                }
                sb.append("}");
                logger.info(sb.toString());
            } else {
                logger.info("sys.conf is empty.");
            }
        } catch (Exception e) {
            logger.error("load sys.conf failed", e);
            throw new RuntimeException("load sys.conf failed!!", e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
            }
        }

    }

    private String getProperty(String key) {
        return props.getProperty(key);
    }

    private static HashMap<String,String> datacentersMapping;

    private synchronized static void initDataCenters() {
        if(datacentersMapping!=null) {
            return;
        }
        datacentersMapping=new HashMap<String,String>();
        String [] Mapping=get("datacenter.mapping") == null ? new String[] {"00:SH"} : get("datacenter.mapping").split(",");
        for(String mp:Mapping) {
            String []ss=mp.split(":");
            datacentersMapping.put(ss[0], ss[1].toUpperCase());
        }
    }

    public static HashMap<String, String> getDatacentersMapping(){
        if (datacentersMapping == null) {
            initDataCenters();
        }
        return datacentersMapping;
    }

    public static void main(String[] args) {
        System.out.println(SysConfUtil.get("sys_p1"));
        System.out.println(SysConfUtil.get("sys_p3_zh"));
        System.out.println(isT());
    }
}
