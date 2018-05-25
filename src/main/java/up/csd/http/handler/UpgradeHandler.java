package up.csd.http.handler;

import up.csd.core.SysLogger;
import up.csd.core.ZDogsCode;
import up.csd.http.server.HttpContext;
import up.csd.http.server.UrlHandler;
import up.csd.json.server.SeverState;
import up.csd.util.ZDogsUtil;

/**
 * Created by Smile on 2018/5/23.
 */
public class UpgradeHandler extends UrlHandler {

    @Override
    public String getDefaultUrl() {
        return "/upgrade";
    }

    @Override
    public void service(HttpContext context) throws Exception {
        String uri = context.httpRequest.uri();

        if (uri.endsWith("?geli")) { // 隔离本服务节点
            SeverState.upgrade();
            ZDogsUtil.sendError(ZDogsCode._9001);

        } else if (uri.endsWith("?huifu")) { // 恢复本服务节点
            SeverState.restore();
            SysLogger.info("节点状态已恢复");

        } else {
            handle404(context);
            return;
        }

        responseText(context, "YES!");
    }
}
