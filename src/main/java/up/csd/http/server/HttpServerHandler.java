package up.csd.http.server;

import java.io.IOException;
import java.util.LinkedHashMap;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import up.csd.core.Counter;
import up.csd.util.LogIdUtil;

/**
 * Created by Smile on 2018/5/23.
 */
@Sharable
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger logger = Logger.getLogger(HttpServerHandler.class);

    private Counter counter;
    private LinkedHashMap<String, UrlFilter> urlFilters = new LinkedHashMap<>();
    private LinkedHashMap<String, UrlHandler> urlHandlers = new LinkedHashMap<>();

    public HttpServerHandler(LinkedHashMap<String, UrlHandler> urlHandlers, Counter counter) {
        this(urlHandlers, null, counter);
    }

    public HttpServerHandler(LinkedHashMap<String, UrlHandler> urlHandlers, LinkedHashMap<String, UrlFilter> urlFilters,
                             Counter counter) {
        if (urlHandlers != null && urlHandlers.size() > 0) {
            this.urlHandlers = urlHandlers;
        }
        if (urlFilters != null && urlFilters.size() > 0) {
            this.urlFilters = urlFilters;
        }
        this.counter = counter;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        counter.incConn();
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        counter.decConn();
        super.channelInactive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {

        HttpContext context = new HttpContext(ctx, request, counter);

        try {
            String uri = request.uri();
            logger.info("Http " + request.method() + " " + uri);

            // 1. behave like servlet filter
            for (String uriPrefix : urlFilters.keySet()) {
                if (uri.startsWith(uriPrefix)) {
                    if(logger.isDebugEnabled()) {
                        logger.debug("mapping filter : " + uriPrefix);
                    }
                    UrlFilter filter = urlFilters.get(uriPrefix);
                    logger.info(filter.toString()); // TODO do something, Not Implemented!
                    return;
                }
            }

            // 2. urlHandler behaves like a servlet
            for (String uriPrefix : urlHandlers.keySet()) {
                if (uri.startsWith(uriPrefix)) {
                    if(logger.isDebugEnabled()) {
                        logger.debug("mapping handler : " + uriPrefix);
                    }
                    UrlHandler handler = urlHandlers.get(uriPrefix);
                    handler.service(context);
                    return;
                }
            }

            logger.warn("unknown uri");
            UrlHandler.handle404(context);

        } finally {
            LogIdUtil.removeMdc();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof IOException &&
                StringUtils.startsWithAny(cause.getMessage(),
                        new String[] {"远程主机强迫关闭了一个现有的连接", "Connection reset by peer"})) {
            // do nothing while client closed connection.
        } else {
            logger.warn("exceptionCaught by " + getClass().getName(), cause);
        }

        ctx.close();
    }
}
