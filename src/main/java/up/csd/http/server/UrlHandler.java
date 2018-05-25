package up.csd.http.server;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.apache.log4j.Logger;
import up.csd.core.EasyMap;
import up.csd.util.AntPathMatcher;
import up.csd.util.LogIdUtil;

import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by Smile on 2018/5/23.
 */
public abstract class UrlHandler {

    protected static final Logger logger = Logger.getLogger(UrlHandler.class);

    public abstract void service(Http2JsonContext context) throws Exception;


    private final Map<HttpMethod, Map<String, Func>> funcs = new HashMap<>();
    /**
     * 按照绑定顺序依次匹配处理函数，一旦匹配上，就使用该函数处理。
     * @param method
     * @param uriPattern
     * @param func
     */
    public void bindFunction(HttpMethod method, String uriPattern, Func func) {
        Map<String, Func> funcsOfMethod = funcs.get(method);
        if (funcsOfMethod == null) {
            funcsOfMethod = new LinkedHashMap<>();
            funcs.put(method, funcsOfMethod);
        }
        funcsOfMethod.put(uriPattern, func);
    }
    public void processReq(Http2JsonContext context, EasyMap reqMap) {
        String uri = context.httpRequest.uri();
        HttpMethod method = context.httpRequest.method();
        Map<String, Func> funcsOfTheMethod = funcs.get(method);
        if (funcsOfTheMethod == null) {
            handle405(context, method);
            return;
        }
        for (String uriPattern : funcsOfTheMethod.keySet()) {
            Func func = funcsOfTheMethod.get(uriPattern);
            Map<String, String> pathVariables = matchUriPattern(uri, uriPattern);
            if (pathVariables != null) {
                func.doIt(context, pathVariables, reqMap);
                return;
            }
        }
        handle404(context);
    }
    static final String VARIABLE_PATTERN = "\\{[^/]+?\\}";
    static final Pattern REG_VARIABLE_PATTERN = Pattern.compile(VARIABLE_PATTERN);
    static final AntPathMatcher pathMatcher = new AntPathMatcher();
    /**
     * try to match the uri with the uriPattern, if match, will try to fetch path variables from the uri.<br>
     * if not match, return null.<br>
     * if no path variable, an empty map will be returned.<br>
     * @param uri
     * @param uriPattern
     * @return if match, return the path varables as a Map<String, String>.
     */
    public static Map<String, String> matchUriPattern(String uri, String uriPattern) {
        if (!REG_VARIABLE_PATTERN.matcher(uriPattern).find()) {
            if (uri.startsWith(uriPattern)) {
                return Collections.emptyMap();
            } else {
                return null; // null means not match
            }
        }
        if (pathMatcher.matchStart(uriPattern, uri)) {
            return fetchPathVariables(uri, uriPattern);
        }

        return null; // null means not match
    }
    private static Map<String, String> fetchPathVariables(String uri, String uriPattern) {
        Map<String, String> pathVariables = pathMatcher.extractUriTemplateVariables(uriPattern, uri);
        return pathVariables;
    }
    public interface Func {
        void doIt(Http2JsonContext context, Map<String, String> pathVariables, EasyMap reqMap);
    }


    public static void convertParamCharset(Map<String, String> params, Charset destCharset) {
        convertParamCharset(params, CharsetUtil.ISO_8859_1, destCharset);
    }

    public static void convertParamCharset(Map<String, String> params, Charset srcCharset, Charset destCharset) {
        for(Map.Entry<String, String> e : params.entrySet()) {
            params.put(e.getKey(), new String(e.getValue().getBytes(srcCharset), destCharset));
        }
    }

    public static Map<String, String> getOnlyFirstParam(Map<String, List<String>> params) {
        Map<String, String> ret = new HashMap<>();
        for(Map.Entry<String, List<String>> entry : params.entrySet()) {
            List<String> values = entry.getValue();
            if (values.size() > 0) {
                ret.put(entry.getKey(), values.get(0));
            }
        }
        return ret;
    }

    public static Map<String, List<String>> parseUrlParams(HttpRequest httpRequest) {
        return parseUrlParams(httpRequest, CharsetUtil.UTF_8);
    }

    public static Map<String, List<String>> parseUrlParams(HttpRequest httpRequest, Charset charset) {
        return new QueryStringDecoder(httpRequest.uri(), charset).parameters();
    }

    public static String parseContentAsString(FullHttpRequest httpRequest, Charset charset) {
        return httpRequest.content().toString(charset);
    }

    public static void handle404(Http2JsonContext context) {
        responseError(context, HttpResponseStatus.NOT_FOUND, "Not Found.");
    }

    public static void handle405(Http2JsonContext context, HttpMethod method) {
        String info = "Method[" + method.name() + "] is not supported!";
        responseError(context, HttpResponseStatus.METHOD_NOT_ALLOWED, info);
    }

    public static void handle502(Http2JsonContext context, String error_des) {
        responseError(context, HttpResponseStatus.BAD_GATEWAY, error_des);
    }

    public static void responseError(Http2JsonContext context, HttpResponseStatus status, String text) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status,
                Unpooled.copiedBuffer(text, CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        sendResponse(context, response);
    }

    public static void responseText(Http2JsonContext context, String textRespContent) {
        responseString(context, textRespContent, "text/plain; charset=UTF-8");
    }

    public static void responseXML(Http2JsonContext context, String textRespContent) {
        responseString(context, textRespContent, "text/xml; charset=UTF-8");
    }

    public static void responseJSON(Http2JsonContext context, HttpResponseStatus status, String jsonResp) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status,
                Unpooled.copiedBuffer(jsonResp, CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");
        sendResponse(context, response);
    }

    public static void responseString(Http2JsonContext context, String textRespContent, String contentType) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                context.httpRequest.decoderResult().isSuccess() ?
                        HttpResponseStatus.OK : HttpResponseStatus.BAD_REQUEST,
                Unpooled.copiedBuffer(textRespContent, CharsetUtil.UTF_8));

        if (textRespContent != null) {
            logger.info("Resp Body[" + textRespContent.replaceAll("\r|\n", "") + "]");
        } else {
            logger.info("Resp Body is null.");
        }

        response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
        sendResponse(context, response);

    }
    public static void sendRedirect(Http2JsonContext context, String newUri) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FOUND);
        response.headers().set(HttpHeaderNames.LOCATION, newUri);
        sendResponse(context, response);
    }

    public static void sendResponse(Http2JsonContext context, FullHttpResponse response) {

        boolean keepAlive = HttpUtil.isKeepAlive(context.httpRequest);
        if (keepAlive) {
            // Add 'Content-Length' header only for a keep-alive connection.
            response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
            // Add keep alive header as per:
            // http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        logger.info("Resp Status[" + response.status().code() + "]");

        // Write the response.
        ChannelFuture future = context.reqChannel.writeAndFlush(response);

        if (!keepAlive) { // close it
            future = context.reqChannel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }

        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                LogIdUtil.setMdc(context.logId);
                long timeUsed = System.currentTimeMillis() - context.startTime;
                context.counter.addTime(timeUsed);
                if (future.isSuccess()) {
                    logger.info("HttpResp complete, duration=" + timeUsed);
                } else {
                    logger.warn("HttpResp failed! " + future.cause(), future.cause());
                }
                LogIdUtil.removeMdc();
            }
        });
    }

    public String getDefaultUrl() {
        return null;
    }
}