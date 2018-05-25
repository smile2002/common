package up.csd.magpie.server;

import com.unionpay.magpie.config.MagpieServerContext;
import com.unionpay.magpie.config.ServiceConfig;
import com.unionpay.magpie.log.ClientLogger;
import com.unionpay.magpie.remoting.support.magpie.MagpieRequest;
import com.unionpay.magpie.remoting.support.magpie.MagpieResponse;
import com.unionpay.magpie.remoting.support.magpie.MagpieStatus;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.log4j.Logger;
import up.csd.core.SysLogger;

/**
 * Created by Smile on 2018/5/25.
 */
@Sharable
public class MagpieHandler extends SimpleChannelInboundHandler<MagpieRequest> {

    private static final Logger logger = Logger.getLogger("sys.log");

    private final MagpieServer server;

    public MagpieHandler(MagpieServer server) {
        this.server = server;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MagpieRequest request) throws Exception {

        if(request.isHeartbeat()) {
            logger.debug("Heartbeat received from" + ctx.channel().remoteAddress());
        } else {
            String serviceId = request.getServiceId();
            ServiceConfig serviceConfig = MagpieServerContext.getServiceConfig(serviceId);
            MagpieResponse response;
            if (serviceConfig == null) {
                ClientLogger.ROOT_LOGGER.serviceNotExist(serviceId);
                response = new MagpieResponse(request);
                response.setStatus(MagpieStatus.SERVICE_NOT_FOUND);
                ctx.writeAndFlush(response);
            } else {
                /** OneWay proto not supported yet **/
                //if (request.isOneWay()) {
                //server.process(ctx, request.getRequestBytes()));
                //} else {
                server.process(ctx, request.getRequestBytes());
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        SysLogger.error("exceptionCaught by JsonHandler, " + cause, cause);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        server.counter.incConn();
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        server.counter.decConn();
        super.channelInactive(ctx);
    }
}