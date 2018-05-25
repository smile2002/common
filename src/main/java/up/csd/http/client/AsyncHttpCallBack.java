package up.csd.http.client;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.concurrent.FutureCallback;

/**
 * Created by Smile on 2018/5/23.
 */
public abstract class AsyncHttpCallBack implements FutureCallback<HttpResponse> {

    private HttpRequestBase httpMethod;

    public HttpRequestBase getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(HttpRequestBase httpMethod) {
        this.httpMethod = httpMethod;
    }
}