package up.csd.json.client;

import java.util.List;

import up.csd.core.SysLogger;
import up.csd.core.ZDogsCode;
import up.csd.json.client.ClientConfig.SvrConf;
import up.csd.util.ZDogsUtil;

/**
 * Created by Smile on 2018/5/22.
 */
public class ClientPool {

    private final SvrConf svrConf;
    private final List<AsyncClient> clientLst;
    private int currIdx = 0;

    public ClientPool(SvrConf svrConf, List<AsyncClient> clientLst) {
        this.svrConf = svrConf;
        this.clientLst = clientLst;
    }

    public AsyncClient chooseClient() {
        if (clientLst == null || clientLst.size() == 0) {
            SysLogger.error("[" + svrConf.getSvrNm() + "]无可用链路");
            ZDogsUtil.sendError(ZDogsCode._2001);
            return null;
        }
        synchronized (clientLst) {
            for (int i = 0; i < clientLst.size(); i ++) {
                AsyncClient client = clientLst.get(currIdx);
                if (currIdx == clientLst.size() - 1) {
                    currIdx = 0;
                } else {
                    currIdx ++;
                }
                if (client.isAvailable()) {
                    return client;
                }
            }
        }
        SysLogger.error("[" + svrConf.getSvrNm() + "]无可用链路");
        ZDogsUtil.sendError(ZDogsCode._2001);
        return null;
    }

    /**
     * 如果有一个可用客户端，则认为该客户端池可用
     * @return
     */
    public boolean isAvailable() {
        if (clientLst == null || clientLst.size() == 0) {
            return false;
        }
        for (AsyncClient client : clientLst) {
            if (client.isAvailable()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 心跳
     */
    public synchronized void ping() {
        if (clientLst != null) {
            for (AsyncClient client : clientLst) {
                client.ping();
            }
        }
    }

    /**
     * 重连
     */
    public synchronized void reconnect() {
        if (clientLst != null) {
            for (AsyncClient client : clientLst) {
                client.reconnect();
            }
        }
    }

    /**
     * 监控
     */
    public synchronized void monitor() {
        if (clientLst != null) {
            for (AsyncClient client : clientLst) {
                client.monitor();
            }
        }
    }

    public SvrConf getSvrConf() {
        return svrConf;
    }

    @Override
    public String toString() {
        return "ClientPool[" + svrConf.getSvrNm() + "@" + svrConf.getAddrLst() + "]";
    }
}