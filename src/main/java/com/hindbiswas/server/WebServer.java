package com.hindbiswas.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebServer {
    private final int port;
    private final ExecutorService pool;

    public WebServer() {
        this.port = 8080;
        this.pool = Executors.newFixedThreadPool(10);
    }

    public WebServer(int port) {
        this.port = port;
        this.pool = Executors.newFixedThreadPool(10);
    }

    public WebServer(int port, int maxThreads) {
        this.port = port;
        this.pool = Executors.newFixedThreadPool(maxThreads);
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);
            while (true) {
                Socket socket = serverSocket.accept();
                pool.submit(new ConnectionHandler(socket));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
