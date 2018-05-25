package sandbox;

import io.netty.channel.ChannelHandlerContext;
import up.csd.core.EasyMap;
import up.csd.json.codec.Message;
import up.csd.json.server.AbstractJsonProcessor;
import up.csd.json.server.PingProcessor;
import up.csd.json.server.SeverState;
import up.csd.json.server.JsonServer;
import org.apache.log4j.Logger;
import up.csd.util.MiscUtil;

/**
 * Created by Smile on 2018/5/22.
 */
public class BottomServer {

    private static Logger logger = Logger.getLogger("sys.log");


    public static void main(String[] args) throws Exception{
        JsonServer server = new JsonServer(9600, "Essential Server");
        server.setMaxThreads(50);
        server.bindProcessor("ping", PingProcessor.INSTANCE);
        server.bindProcessor("bill_no", new AbstractJsonProcessor() {
            @Override
            public void process(ChannelHandlerContext ctx, Message reqMap) {
                EasyMap respMap = new EasyMap();
                respMap.put("result", "0");
                respMap.put("resultString", "OK");
                respMap.put("billNo", 12345);
                ctx.writeAndFlush(reqMap.buildRespMessage(respMap));
            }
        });
        server.startup(false);
        //addShutdownHook();

        while(true) {
            Thread.sleep(5);
        }
    }

    public static void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                logger.info("ShutdownHook triggled for BizServer.");

                SeverState.upgrade();
                logger.info("Update server state to " + SeverState.getPongType());

                logger.info("Wait for all trasactions to finish.");
                MiscUtil.waitFor(10000);
            }
        }, "BizServerShutdownHook"));
    }
}

