package up.csd.json.server;

import io.netty.channel.ChannelHandlerContext;
import up.csd.core.EasyMap;
import up.csd.json.codec.Message;

import java.text.NumberFormat;

/**
 * Created by Smile on 2018/5/22.
 */
public class AdminProcessor extends AbstractJsonProcessor {

    public static final AdminProcessor INSTANCE = new AdminProcessor();

    @Override
    public void process(ChannelHandlerContext ctx, Message req) {

        long startTime = System.currentTimeMillis();

        EasyMap reqMap = req.toEasyMap();

        logger.info("reqMap:" + reqMap);

        EasyMap respMap = new EasyMap();
        respMap.put("result", "0");

        String mngCmd = reqMap.getAsString("mng_op");
        switch (mngCmd) {
            case "upgrade":
                SeverState.upgrade();
                break;
            case "restore":
                SeverState.restore();
                break;
            case "monitor":
                monitor(respMap);
                break;
            default:
                break;
        }

        sendResp(ctx, respMap, req, startTime);
    }

    /**
     * 监控节点当前状态
     * @param respMap
     */
    private void monitor(EasyMap respMap) {
        // 当前节点状态
        respMap.put("pong_type", SeverState.getPongType());

        // 内存使用情况
        Runtime runtime = Runtime.getRuntime();
        long max = runtime.maxMemory();
        long total = runtime.totalMemory();
        long free = runtime.freeMemory();
        long used = total - free;
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        String usedRate = nf.format(used / max * 100);
        respMap.put("memory", usedRate + "(" + used + "/" + max +  ")");

        // CPU使用情况
        respMap.put("cpu", String.valueOf(runtime.availableProcessors()));
    }
}
