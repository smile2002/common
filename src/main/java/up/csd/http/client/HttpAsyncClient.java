package up.csd.http.client;

/**
 * Created by Smile on 2018/5/23.
 */
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.nio.charset.CodingErrorAction;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;

import com.unionpay.common.util.StringUtil;
import org.apache.http.Consts;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.MalformedChallengeException;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Lookup;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.impl.auth.*;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.conn.NoopIOSessionStrategy;
import org.apache.http.nio.conn.SchemeIOSessionStrategy;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.ssl.SSLContextBuilder;
import up.csd.core.NamedThreadFactory;
import up.csd.core.SysLogger;
import up.csd.util.SysConfUtil;


public class HttpAsyncClient {

    private static int soTimeout = 15000;//

    private static int connectTimeout = 15000;//

    private static int connectRequestTimeout = 15000;

    private static int poolSize = 150000;//

    private static int maxPerRoute = 50000;//

    private PoolingNHttpClientConnectionManager conMgr;

    public PoolingNHttpClientConnectionManager getConMgr() {
        return conMgr;
    }

    public void setConMgr(PoolingNHttpClientConnectionManager conMgr) {
        this.conMgr = conMgr;
    }

    private CloseableHttpAsyncClient asyncHttpClient;

    public HttpAsyncClient() {
        try {
            this.asyncHttpClient = createAsyncClient();
        } catch (Exception e) {
            throw new RuntimeException("Create HttpAsyncClient error!", e);
        }

    }

    @SuppressWarnings("deprecation")
    private CloseableHttpAsyncClient createAsyncClient()
            throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException,
            MalformedChallengeException, IOReactorException {
//		RequestConfig requestConfig = RequestConfig.custom()
//				.setConnectTimeout(connectTimeout)
//				.setSocketTimeout(socketTimeout).build();

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(connectTimeout)
                .setConnectionRequestTimeout(connectRequestTimeout)
                .setSocketTimeout(soTimeout).build();

//		SSLContext sslcontext = SSLContexts.createDefault();
        SSLContext sslcontext = SSLContextBuilder.create().useProtocol("TLSv1").build();
        RegistryBuilder<SchemeIOSessionStrategy> regBuilder = RegistryBuilder.<SchemeIOSessionStrategy>create()
                .register("http", NoopIOSessionStrategy.INSTANCE)
                .register("https", new SSLIOSessionStrategy(sslcontext));

        boolean trustAll = false;
        String httpsTrustAll = SysConfUtil.get("https.trust.all");
        if (!StringUtil.isBlank(httpsTrustAll)) {
            try {
                trustAll = Boolean.valueOf(httpsTrustAll);
            } catch (Exception e) {
                trustAll = false;
                SysLogger.error("parse https.trust.all failed:" + httpsTrustAll);
            }
        }

        if (trustAll) {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                            // don't check
                        }
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                            // don't check
                        }
                    }
            };
            sslcontext.init(null, trustAllCerts, null);
            regBuilder.register("https", new SSLIOSessionStrategy(sslcontext,SSLIOSessionStrategy.ALLOW_ALL_HOSTNAME_VERIFIER));
        }

        Registry<SchemeIOSessionStrategy> sessionStrategyRegistry = regBuilder.build();

        //
        IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                .setIoThreadCount(Runtime.getRuntime().availableProcessors()*2)
                .setSoKeepAlive(true)
                .setSoLinger(0)
                .setConnectTimeout(connectTimeout)
                .setSoTimeout(soTimeout)
                .setTcpNoDelay(true)
                .setSoReuseAddress(true)
                .build();
        //.setIoThreadCount(Runtime.getRuntime().availableProcessors()*2).build();
        //
        ConnectingIOReactor ioReactor;

        ioReactor = new DefaultConnectingIOReactor(ioReactorConfig);
        conMgr = new PoolingNHttpClientConnectionManager(
                ioReactor,
                null,
                sessionStrategyRegistry,
                null
//				,
//				null,
//				3,
//				TimeUnit.SECONDS
        );

        if (poolSize > 0) {
            conMgr.setMaxTotal(poolSize);
        }

        if (maxPerRoute > 0) {
            conMgr.setDefaultMaxPerRoute(maxPerRoute);
        } else {
            conMgr.setDefaultMaxPerRoute(10);
        }

        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setMalformedInputAction(CodingErrorAction.IGNORE)
                .setUnmappableInputAction(CodingErrorAction.IGNORE)
                .setCharset(Consts.UTF_8)
                .build();

        conMgr.setDefaultConnectionConfig(connectionConfig);

        Lookup<AuthSchemeProvider> authSchemeRegistry = RegistryBuilder.<AuthSchemeProvider>create()
                .register(AuthSchemes.BASIC, new BasicSchemeFactory())
                .register(AuthSchemes.DIGEST, new DigestSchemeFactory())
                .register(AuthSchemes.NTLM, new NTLMSchemeFactory())
                .register(AuthSchemes.SPNEGO, new SPNegoSchemeFactory())
                .register(AuthSchemes.KERBEROS, new KerberosSchemeFactory()).build();

//		logger.info(ioReactorConfig.toString());
//		logger.info(requestConfig.toString());

        return HttpAsyncClients.custom().setConnectionManager(conMgr)
                .setDefaultAuthSchemeRegistry(authSchemeRegistry)
//				.setKeepAliveStrategy(MyConnectionKeepAliveStrategy.INSTANCE)
                .setDefaultCookieStore(new BasicCookieStore())
                .setDefaultRequestConfig(requestConfig)
                .setThreadFactory(new NamedThreadFactory("AsyncHttpSend"))
//				.addInterceptorLast(new RequestAcceptEncoding())
                .build();
    }

    public CloseableHttpAsyncClient getAsyncHttpClient() {
        return asyncHttpClient;
    }
}
