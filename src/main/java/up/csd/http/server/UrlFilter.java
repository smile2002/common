package up.csd.http.server;

/**
 * Created by Smile on 2018/5/23.
 */
public abstract class UrlFilter {
    public abstract void filter(Http2JsonContext context) throws Exception;
}