package up.csd.http.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.apache.commons.lang.StringUtils;
import up.csd.core.Counter;
import up.csd.core.PromiseFailHandler;
import up.csd.core.TcpServer;
import up.csd.http.handler.PrintCountHandler;

import java.util.LinkedHashMap;

/**
 * Created by Smile on 2018/5/23.
 */
public class HttpServer extends TcpServer {
    public Counter counter;

    public static int maxThreads = 1000;
    public static int maxContentLength = 1024 * 1024;

    private LinkedHashMap<String, UrlFilter> urlFilters = new LinkedHashMap<>();
    private LinkedHashMap<String, UrlHandler> urlHandlers = new LinkedHashMap<>();

    public HttpServer(int port) {
        this(port, "MyHttpServer");
    }

    public HttpServer(int port, String name) {
        super(port, name);
        counter = new Counter(name);
    }

    /**
     * 使用handler的默认URL添加UrlHandler
     * @param handler
     * @see up.csd.http.server.HttpServer#bind(String urlPrefix, UrlHandler handler)
     */
    public void bind(UrlHandler handler) {
        String defaultUrl = handler.getDefaultUrl();
        if (StringUtils.isBlank(defaultUrl)) {
            throw new IllegalArgumentException("Handler[" + handler + "] does not have a default url!");
        }
        bind(handler.getDefaultUrl(), handler);
    }

    /**
     * 根据URL前缀添加UrlHandler
     * @param urlPrefix
     * @param handler
     * @see up.csd.http.server.HttpServer#bind(UrlHandler handler)
     */
    public void bind(String urlPrefix, UrlHandler handler) {
        if (handler instanceof PrintCountHandler) {
            // 如果添加了PrintCountHandler，则默认启动counter
            counter.start();
        }
        urlHandlers.put(urlPrefix, handler);
    }

    public void addUrlFilter(String urlPrefix, UrlFilter filter) {
        urlFilters.put(urlPrefix, filter);
    }
    @Override
    public void startup() throws Exception {

        HttpServerHandler httpServerHandler = new HttpServerHandler(urlHandlers, urlFilters, counter);

        this.setChannelInitializer(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline p = ch.pipeline();
                p.addLast(new HttpRequestDecoder());
                p.addLast(new HttpObjectAggregator(maxContentLength)); // don't want to handle HttpChunks
                p.addLast(new HttpResponseEncoder());
//		        p.addLast(new HttpServerExpectContinueHandler()); // TODO 研究如何实现 expect: 100-continue
                p.addLast(new HttpContentCompressor()); // automatic content compression
                p.addLast(PromiseFailHandler.INSTANCE); // 统一处理写出数据失败的情况
                p.addLast(httpServerHandler);
            }
        });
        super.startup();
    }

    public void sync() {
        synchronized(this) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
