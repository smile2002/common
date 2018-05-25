package up.csd.json.client;

/**
 * Created by Smile on 2018/5/22.
 */

import org.apache.commons.lang.Validate;
import up.csd.core.SysLogger;
import up.csd.util.AssertUtil;
import up.csd.util.JsonUtil;
import up.csd.util.StringUtil;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ClientConfig {
    private static final AtomicBoolean notInitialized = new AtomicBoolean(true);

    private static ClientConfig INSTANCE;

    public static final String AT = "@";

    private final List<SvrConf> svrConfLst = new ArrayList<SvrConf>();

    private ConfInfo commonConf;

    private ClientConfig() {
        this.init();
    }

    private ClientConfig(Object file) {
        this.init(file);
    }

    public static ClientConfig getInstance() {
        loadConfig(null);
        return INSTANCE;
    }

    public static void loadConfig(Object fileOrPath) {
        if (INSTANCE == null) {
            synchronized (ClientConfig.class) {
                if (notInitialized.getAndSet(false)) {
                    if (fileOrPath == null) {
                        INSTANCE = new ClientConfig();
                    } else {
                        INSTANCE = new ClientConfig(fileOrPath);
                    }
                }
            }
        }
    }

    private void init() {
        SysLogger.info("载入配置文件开始");

        // 从指定位置读取
        String confDir = System.getProperty("conf.root.dir");
        if (confDir != null && confDir.length() > 0) {
            if (!confDir.endsWith(File.separator)) {
                confDir = confDir + File.separator;
            }
            String filePath = confDir + "json_client.json";
            SysLogger.info("从指定位置读取配置开始: " + filePath);
            loadFile(filePath);
        }

        // 如果未指定路径，则从classpath默认路径读取
        if (commonConf == null) {
            SysLogger.info("从classpath默认路径[/json_client.json]读取配置开始.");
            commonConf = JsonUtil.fromStream(getClass().getResourceAsStream("/json_client.json"), ConfInfo.class);
        }

        parseConf();
    }

    private void init(Object fileOrPath) {
        Validate.notNull(fileOrPath);

        SysLogger.info("载入配置文件开始");

        loadFile(fileOrPath);
        if (commonConf == null) {
            throw new RuntimeException("未在指定的路径找到配置: " + fileOrPath);
        }

        parseConf();
    }

    private void loadFile(Object fileOrPath) {
        Validate.notNull(fileOrPath, "conf path is null.");
        File f = null;
        if (fileOrPath instanceof File) {
            f = (File) fileOrPath;
        } else if (fileOrPath instanceof String) {
            f = new File((String) fileOrPath);
        }
        if (f != null && !f.isFile()) {
            SysLogger.warn("ConfigFile[" + fileOrPath + "] is not a file!");
            return;
        }
        if (f != null && !f.exists()) {
            SysLogger.warn("ConfigFile[" + fileOrPath + "] does not exist!");
            return;
        }
        if (f != null) {
            try {
                commonConf = JsonUtil.fromFile(f, ConfInfo.class);
                SysLogger.info("从指定文件读取配置完成: " + fileOrPath);
                return;
            } catch (Exception e) {
                throw new RuntimeException("load json_client.json failed:" + fileOrPath);
            }
        }
        if (fileOrPath instanceof URL) {
            try {
                commonConf = JsonUtil.fromURL((URL) fileOrPath, ConfInfo.class);
                SysLogger.info("从指定位置读取配置完成: " + fileOrPath);
                return;
            } catch (Exception e) {
                throw new RuntimeException("load json_client.json failed:" + fileOrPath);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private void parseConf() {
        // 检查配置
        AssertUtil.strIsNotBlank(commonConf.getVersion(), "version is empty!");
        AssertUtil.strIsNotBlank(commonConf.getMagicNum(), "magicNum is empty!");
        AssertUtil.strIsNotBlank(commonConf.getClientId(), "clientId is empty!");
        AssertUtil.strIsNotBlank(commonConf.getUseBigEndian(), "useBigEndian is empty!");
        AssertUtil.strIsNotBlank(commonConf.getPingPeriod(), "pingPeriod is empty!");
        AssertUtil.strIsNotBlank(commonConf.getReconnectPeriod(), "reconnectPeriod is empty!");
        AssertUtil.strIsNotBlank(commonConf.getDefaultDataCenter(), "defaultDataCenter is empty!");
        AssertUtil.collectionIsNotEmpty(commonConf.getSvrConfLst(), "server config is empty!!");

        List<Map> svrConfMapLst = commonConf.getSvrConfLst();
        Set<String> allConfSvrKeySet = new HashSet<>();
        for (Map svrConfMap : svrConfMapLst) {
            if (!svrConfMap.containsKey("svrNm")) {
                throw new RuntimeException("svrNm is empty!");
            }
            if (!svrConfMap.containsKey("maxReqNum")) {
                throw new RuntimeException("maxReqNum is empty!");
            }
            if (!svrConfMap.containsKey("cmdLst")) {
                throw new RuntimeException("addrLst is empty!");
            }
            if (!svrConfMap.containsKey("addrLst")) {
                throw new RuntimeException("addrLst is empty!");
            }

            // 服务器配置
            SvrConf svrConf = new SvrConf();

            // 读取服务名
            String svrNm = String.valueOf(svrConfMap.get("svrNm"));
            AssertUtil.strIsNotBlank(svrNm, "svrNm is blank.");
            svrConf.setSvrNm(svrNm);

            // 读取最大请求数配置
            String maxReqNumStr = String.valueOf(svrConfMap.get("maxReqNum"));
            AssertUtil.strIsNotBlank(maxReqNumStr, "maxReqNum is blank.");
            svrConf.setMaxReqNum(Integer.parseInt(maxReqNumStr));

            // 读取最大同时存在的心跳请求次数配置
            // 如果发送的心跳次数超过这个配置则认为链路超时
            // 默认1次心跳超时就隔离链路
            if (!svrConfMap.containsKey("maxPingNum")) {
                svrConf.setMaxPingNum(1);
            } else {
                svrConf.setMaxPingNum(Integer.parseInt(String.valueOf(svrConfMap.get("maxPingNum"))));
            }

            // 读取心跳执行周期配置
            // 如果未单独配置，则使用通用配置
            if (svrConfMap.get("pingPeriod") == null) {
                svrConf.setPingPeriod(Integer.parseInt(commonConf.getPingPeriod()));
            } else {
                svrConf.setPingPeriod(Integer.parseInt(String.valueOf(svrConfMap.get("pingPeriod"))));
            }

            // 读取连接数配置
            // 如果未配置连接数，则默认与每个链路只建立一条连接
            if (svrConfMap.get("connNum") == null) {
                svrConf.setConnNum(1);
            } else {
                svrConf.setConnNum(Integer.parseInt(String.valueOf(svrConfMap.get("connNum"))));
            }

            // 读取命令字所属数据中心
            String dataCenter = null;
            if (svrConfMap.containsKey("dataCenter")) {
                dataCenter = String.valueOf(svrConfMap.get("dataCenter"));
            } else {
                dataCenter = commonConf.getDefaultDataCenter();
            }
            svrConf.setDataCenter(dataCenter);

            // 读取业务请求超时最大个数配置 - 用于判断链路是否超时
            if (!svrConfMap.containsKey("maxTimeOutReqNum")) {
                if (!StringUtil.isBlank(commonConf.getMaxTimeOutReqNum())) {
                    svrConf.setMaxTimeOutReqNum(Integer.parseInt(commonConf.getMaxTimeOutReqNum()));
                } else {
                    svrConf.setMaxTimeOutReqNum(0);
                }
            } else {
                svrConf.setMaxTimeOutReqNum(Integer.parseInt(String.valueOf(svrConfMap.get("maxTimeOutReqNum"))));
            }

            // 读取命令字列表
            if (svrConfMap.get("cmdLst") == null) {
                throw new RuntimeException("cmdLst is blank.");
            }
            String cmdLstStr = String.valueOf(svrConfMap.get("cmdLst"));
            AssertUtil.strIsNotBlank(cmdLstStr, "cmdLst is blank.");
            String[] cmdArr = cmdLstStr.split(",");
            Set<String> cmdSet = new HashSet<>();
            for (String cmd : cmdArr) {
                String svrConfKey = StringUtil.trim(cmd) + AT + dataCenter;
                if (allConfSvrKeySet.contains(svrConfKey)) {
                    throw new RuntimeException("duplicate cmd:" + svrConfKey);
                }
                allConfSvrKeySet.add(svrConfKey);
                cmdSet.add(cmd);
            }
            svrConf.setCmdSet(cmdSet);

            // 读取地址列表
            String addrLstStr = String.valueOf(svrConfMap.get("addrLst"));
            AssertUtil.strIsNotBlank(addrLstStr, "addrLst is blank.");
            String[] addrArr = addrLstStr.split(",");
            Set<String> addrSet = new HashSet<>();
            for (String addr : addrArr) {
                addrSet.add(StringUtil.trim(addr));
            }
            svrConf.setAddrSet(addrSet);
            svrConf.setAddrLst(addrLstStr);

            // 加入服务器配置列表
            svrConfLst.add(svrConf);
        }

        SysLogger.info("\nRAW CONFIG:\n" + JsonUtil.toFormattedJson(commonConf));
        SysLogger.info("\nsvrConfLst:\n" + JsonUtil.toFormattedJson(svrConfLst));

        SysLogger.info("载入配置文件完成");
    }

    @SuppressWarnings("rawtypes")
    public static class ConfInfo {

        private String version;
        private String magicNum;
        private String clientId;
        private String useBigEndian;
        private String pingPeriod; // 心跳任务执行周期
        private String reconnectPeriod; // 重连任务执行周期
        private String defaultDataCenter;
        private String maxTimeOutReqNum; // 业务请求超时最大个数

        private List<Map> svrConfLst;

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getMagicNum() {
            return magicNum;
        }

        public void setMagicNum(String magicNum) {
            this.magicNum = magicNum;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public List<Map> getSvrConfLst() {
            return svrConfLst;
        }

        public void setSvrConfLst(List<Map> svrConfLst) {
            this.svrConfLst = svrConfLst;
        }

        public String getUseBigEndian() {
            return useBigEndian;
        }

        public void setUseBigEndian(String useBigEndian) {
            this.useBigEndian = useBigEndian;
        }

        public String getPingPeriod() {
            return pingPeriod;
        }

        public void setPingPeriod(String pingPeriod) {
            this.pingPeriod = pingPeriod;
        }

        public String getReconnectPeriod() {
            return reconnectPeriod;
        }

        public void setReconnectPeriod(String reconnectPeriod) {
            this.reconnectPeriod = reconnectPeriod;
        }

        public String getDefaultDataCenter() {
            return defaultDataCenter;
        }

        public void setDefaultDataCenter(String defaultDataCenter) {
            this.defaultDataCenter = defaultDataCenter;
        }

        public String getMaxTimeOutReqNum() {
            return maxTimeOutReqNum;
        }

        public void setMaxTimeOutReqNum(String maxTimeOutReqNum) {
            this.maxTimeOutReqNum = maxTimeOutReqNum;
        }
    }

    public class SvrConf {

        private String svrNm;
        private int maxReqNum;
        private int connNum;
        private int maxPingNum;
        private int pingPeriod;
        private String dataCenter;
        private int readTimeOut;
        private int maxTimeOutReqNum;
        private Set<String> cmdSet;
        private Set<String> addrSet;
        private String addrLst;

        public String getSvrNm() {
            return svrNm;
        }

        public void setSvrNm(String svrNm) {
            this.svrNm = svrNm;
        }

        public Set<String> getCmdSet() {
            return cmdSet;
        }

        public void setCmdSet(Set<String> cmdSet) {
            this.cmdSet = cmdSet;
        }

        public Set<String> getAddrSet() {
            return addrSet;
        }

        public void setAddrSet(Set<String> addrSet) {
            this.addrSet = addrSet;
        }

        public String getAddrLst() {
            return addrLst;
        }

        public void setAddrLst(String addrLst) {
            this.addrLst = addrLst;
        }

        public int getMaxReqNum() {
            return maxReqNum;
        }

        public void setMaxReqNum(int maxReqNum) {
            this.maxReqNum = maxReqNum;
        }

        public int getConnNum() {
            return connNum;
        }

        public void setConnNum(int connNum) {
            this.connNum = connNum;
        }

        public int getMaxPingNum() {
            return maxPingNum;
        }

        public void setMaxPingNum(int maxPingNum) {
            this.maxPingNum = maxPingNum;
        }

        public int getPingPeriod() {
            return pingPeriod;
        }

        public void setPingPeriod(int pingPeriod) {
            this.pingPeriod = pingPeriod;
        }

        public String getDataCenter() {
            return dataCenter;
        }

        public void setDataCenter(String dataCenter) {
            this.dataCenter = dataCenter;
        }

        public int getReadTimeOut() {
            return readTimeOut;
        }

        public void setReadTimeOut(int readTimeOut) {
            this.readTimeOut = readTimeOut;
        }

        public int getMaxTimeOutReqNum() {
            return maxTimeOutReqNum;
        }

        public void setMaxTimeOutReqNum(int maxTimeOutReqNum) {
            this.maxTimeOutReqNum = maxTimeOutReqNum;
        }

    }

    public int getVersion() {
        return Integer.parseInt(this.commonConf.getVersion());
    }

    public long getMagicNum() {
        return Long.parseLong(this.commonConf.getMagicNum());
    }

    public String getClientId() {
        return this.commonConf.getClientId();
    }

    public boolean isUseBigEndian() {
        return Boolean.valueOf(this.commonConf.getUseBigEndian());
    }

    public int getPingPeriod() {
        return Integer.parseInt(this.commonConf.getPingPeriod());
    }

    public int getReconnectPeriod() {
        return Integer.parseInt(this.commonConf.getReconnectPeriod());
    }

    public String getDefaultDataCenter() {
        return this.commonConf.getDefaultDataCenter();
    }

    public List<SvrConf> getSvrConfLst() {
        return Collections.unmodifiableList(svrConfLst);
    }

}
