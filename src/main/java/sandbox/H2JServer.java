package sandbox;

import up.csd.http.server.HttpServer;

/**
 * Created by Smile on 2018/5/23.
 */
public class H2JServer {
    public static void main(String[] args) throws Exception {
        HttpServer httpServer = new HttpServer(8700, "rcv-http");
        httpServer.bind("/test", new H2JHandler());
        httpServer.startup();
        while(true) {
            Thread.sleep(5);
        }
    }
}
