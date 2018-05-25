package up.csd.http.client;

import com.unionpay.common.util.StringUtil;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.apache.http.message.BasicNameValuePair;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by Smile on 2018/5/23.
 */
public class  HttpClientService {

    private static final Logger logger = Logger.getLogger(HttpClientService.class);

    /**
     * 异步发起HTTP请求，未自定义Header
     * @param baseUrl
     * @param isPost
     * @param urlParams
     * @param postBody
     * @param charset
     * @param callback
     * @throws Exception
     */
    public static void exeAsyncReq(String logPrefix, String baseUrl, boolean isPost, List<BasicNameValuePair> urlParams, String postBody,
                                   String charset, AsyncHttpCallBack callback) throws Exception {
        exeAsyncReqWithHeader(logPrefix, baseUrl, isPost, urlParams, null, postBody, charset, null, callback);
    }

    /**
     * 异步发起HTTP请求，可自定义Header
     * @param baseUrl
     * @param isPost
     * @param urlParams
     * @param headerMap
     * @param postBody
     * @param charset
     * @param callback
     * @throws Exception
     */
    public static void exeAsyncReqWithHeader(String logPrefix, String baseUrl, boolean isPost, List<BasicNameValuePair> urlParams,
                                             Map<String, String> headerMap, String postBody, String charset, String ContentType, AsyncHttpCallBack callback) throws Exception {
        if (baseUrl == null) {
            logger.error("we don't have base url, check config");
            throw new RuntimeException("missing base url");
        }

        HttpRequestBase httpMethod;
        CloseableHttpAsyncClient hc = null;
        try {
            hc = HttpClientFactory.getInstance().getPooledHttpAsyncClient().getAsyncHttpClient();
            // AsynES.connAdd();
            if (!hc.isRunning()) {
                hc.start();
            }
            // HttpClientContext localContext = HttpClientContext.create();
            // BasicCookieStore cookieStore = new BasicCookieStore();

            if (isPost) {
                httpMethod = new HttpPost(baseUrl);
                if (null != urlParams) {
                    String getUrl = EntityUtils.toString(new UrlEncodedFormEntity(urlParams, charset));
                    httpMethod.setURI(new URI(httpMethod.getURI().toString() + "?" + getUrl));
                }
                logger.info(logPrefix + "-url=[" + httpMethod.getURI() + "]");

                if (null != postBody) {
                    StringEntity entity = new StringEntity(postBody, charset);
                    ((HttpPost) httpMethod).setEntity(entity);
                }
                logger.info(logPrefix + "-body=[" + (postBody == null ? "" : postBody.replaceAll("\r|\n", "")) + "]");
            } else {
                httpMethod = new HttpGet(baseUrl);
                if (null != urlParams) {
                    String getUrl = EntityUtils.toString(new UrlEncodedFormEntity(urlParams, charset));
                    httpMethod.setURI(new URI(httpMethod.getURI().toString() + "?" + getUrl));
                }
                logger.info(logPrefix + "-url=" + httpMethod.getURI());
            }

            httpMethod.setHeader("Connection", "Keep-Alive");
            if (!StringUtil.isBlank(ContentType)) {
                httpMethod.setHeader("Content-type", ContentType);
            } else {
                httpMethod.setHeader("Content-type", "application/x-www-form-urlencoded;charset=" + charset);
            }

            httpMethod.setHeader("User-Agent", "HTTP@95516");
            httpMethod.setHeader("Accept", "text/xml,text/javascript,text/html,application/x-javascript,application/json,application/xml");
            // 设定自定义头部
            if (headerMap != null && !headerMap.isEmpty()) {
                for (String headerKey : headerMap.keySet()) {
                    httpMethod.setHeader(headerKey, headerMap.get(headerKey));
                }
            }
            callback.setHttpMethod(httpMethod);
            //
            logger.info(logPrefix + "-header=" + Arrays.toString(httpMethod.getAllHeaders()));
            // logger.info("Send http req body[" + postBody + "] to url[" +
            // httpMethod.getURI() + "]");
            hc.execute(httpMethod, callback);
        } catch (Exception e) {
            logger.error("Send http req body[" + postBody + "] to url[" + baseUrl + "] error", e);
            throw new RuntimeException("Send http req body error.");
        }
    }
}