package up.csd.json.client;

import up.csd.core.NamedThreadFactory;
import up.csd.core.SysLogger;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import up.csd.core.ZDogsCode;
import up.csd.json.client.ClientConfig.SvrConf;
import up.csd.util.AssertUtil;
import up.csd.util.ZDogsUtil;

/**
 * Created by Smile on 2018/5/22.
 */
public final class ClientPoolFactory {

    private static class ClientPoolFactoryHolder {
        private static final ClientPoolFactory INSTANCE = new ClientPoolFactory();
    }

    private Map<String, ClientPool> clientPoolMap = new HashMap<String, ClientPool>();

    private ClientPoolFactory() {
        init();
    }

    public static ClientPoolFactory getInstance() {
        return ClientPoolFactoryHolder.INSTANCE;
    }

    private void init() {
        SysLogger.info("---ClientPoolFactory init started----");

        ClientConfig conf = ClientConfig.getInstance();
        List<SvrConf> svrConfLst = conf.getSvrConfLst();

        List<ClientPool> poolLst = new ArrayList<>();
        for (SvrConf svrConf : svrConfLst) {
            Set<String> addrSet = svrConf.getAddrSet();
            List<AsyncClient> clientLst = new ArrayList<AsyncClient>();
            int idx = 0;
            for (int i = 0; i < svrConf.getConnNum(); i++) {
                for (String addr : addrSet) {
                    AsyncClient client = new AsyncClient(addr, idx, svrConf);
                    idx++;
                    clientLst.add(client);
                }
            }
            ClientPool pool = new ClientPool(svrConf, clientLst);

            poolLst.add(pool);

            // 构建命令字@数据中心@分组ID与连接池的映射关系
            Set<String> cmdSet = svrConf.getCmdSet();
            for (String cmd : cmdSet) {
                clientPoolMap.put(cmd + ClientConfig.AT + svrConf.getDataCenter(), pool);
            }
        }

        if (poolLst == null || poolLst.size() == 0) {
            throw new RuntimeException("poolLst is empty");
        }

        SysLogger.info("ClientPool number:" + poolLst.size());

//		int threadNum = Runtime.getRuntime().availableProcessors() + 1;
//		if (svrConfLst.size() > threadNum) {
//			threadNum = svrConfLst.size();
//		}

        // ping 和 重连
        ScheduledExecutorService es1 = Executors.newScheduledThreadPool(1, new NamedThreadFactory("reconnect-task", true));
        ScheduledExecutorService es2 = Executors.newScheduledThreadPool(1, new NamedThreadFactory("ping-task", true));
        for (final ClientPool pool : poolLst) {
            // 启动自动重连任务
            es1.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        pool.reconnect();
                        pool.monitor();
                    } catch (Exception e) {
                        SysLogger.error("pool reconnect error!!", e);
                    }
                }
            }, conf.getReconnectPeriod(), conf.getReconnectPeriod(), TimeUnit.MILLISECONDS);
            SysLogger.info("自动重连任务已启动");

            es2.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        pool.ping();
                        pool.monitor();
                    } catch (Exception e) {
                        SysLogger.error("processor ping error!!", e);
                    }
                }
            }, pool.getSvrConf().getPingPeriod(), pool.getSvrConf().getPingPeriod(), TimeUnit.MILLISECONDS);
            SysLogger.info("心跳检查任务已启动");
        }

        // 判断是否启动成功
        // 启动成功的标准：每个ClientPool至少有一条可用链路
        boolean initFlg = false;
        while (true) {
            try {
                // 每1秒检查是否启动成功
                TimeUnit.MILLISECONDS.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException("thread sleep error", e);
            }

            SysLogger.info("check if ClientPool is available");
            for (ClientPool pool : poolLst) {
                if (pool.isAvailable()) {
                    initFlg = true;
                    continue;
                } else {
                    initFlg = false;
                    ZDogsUtil.sendError(ZDogsCode._2001, pool.getSvrConf().getSvrNm());
                    SysLogger.warn(pool + " is not avalible!");
                    break;
                }
            }

            if (initFlg) {
                SysLogger.info("ClientPoolFactory init succeeded.");
                break;
            } else {
                SysLogger.warn("ClientPoolFactory init not success, wait for reconnect or ping success!");
            }
        }

        SysLogger.info("---ClientPoolFactory init completed----");
    }

    /**
     * 根据cmd@DataCenter@groupId查找客户端池
     * @param cmd
     * @param dataCenter
     * @return
     */
    public ClientPool getClientPool(String cmd, String dataCenter) {
        AssertUtil.objIsNotNull(cmd, "cmd is null.");
        AssertUtil.objIsNotNull(dataCenter, "dataCenter is null.");
        String key = cmd + ClientConfig.AT + dataCenter;
        ClientPool pool = clientPoolMap.get(key);
        AssertUtil.objIsNotNull(pool, "can't find clientPool:" + key);
        return pool;
    }
}