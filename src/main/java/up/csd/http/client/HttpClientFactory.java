package up.csd.http.client;


/**
 * Created by Smile on 2018/5/23.
 */
public class HttpClientFactory {

    private static HttpAsyncClient httpAsyncClient = new HttpAsyncClient();

    private static HttpClientFactory httpClientFactory = new HttpClientFactory();

    private HttpClientFactory() {}

    /**
     *
     * @return
     */
    public static HttpClientFactory getInstance() {
        return httpClientFactory;
    }

    /**
     *
     * @return
     */
    public HttpAsyncClient getPooledHttpAsyncClient() {
        return httpAsyncClient;
    }

}
