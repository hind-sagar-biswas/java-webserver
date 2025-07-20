package com.hindbiswas.server;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {
        int port = 8080;
        WebServer server = new WebServer(port);
        server.start();
    }
}
