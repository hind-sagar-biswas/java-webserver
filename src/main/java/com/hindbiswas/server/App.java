package com.hindbiswas.server;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {
        int port = 8080;
        WebServer server = new WebServer(port, "/home/shinigami/www");
        HybridRouter router = new HybridRouter();

        router.get("/time", request -> {
            return Response.text("Current time: " + System.currentTimeMillis());
        });

        server.setRouter(router);
        server.start();
    }
}
