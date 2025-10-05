package com.hindbiswas.server;

import com.hindbiswas.server.core.WebServer;
import com.hindbiswas.server.http.Response;
import com.hindbiswas.server.routing.HybridRouter;
import com.hindbiswas.server.routing.StaticRouter;

/**
 * Example application demonstrating the Java WebServer.
 */
public class App {
    public static void main(String[] args) {
        int port = 8080;
        WebServer server = new WebServer(port, "/home/shinigami/www");

        // HybridRouter router = new HybridRouter();

        // router.get("/time", _ -> {
        //     return Response.text("Current time: " + System.currentTimeMillis());
        // });
        StaticRouter router = new StaticRouter();

        server.setRouter(router);
        server.start();
    }
}
