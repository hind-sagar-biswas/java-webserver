package com.hindbiswas.server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebServer {
    private final int port;
    private final ExecutorService pool;
    private final File webRoot;
    private volatile Router router = null;
    private volatile boolean running = false;
    private ServerSocket serverSocket;

    public WebServer() {
        this.port = 8080;
        this.webRoot = new File(".");
        this.pool = Executors.newFixedThreadPool(10);
    }

    public WebServer(int port) {
        this.port = port;
        this.webRoot = new File(".");
        this.pool = Executors.newFixedThreadPool(10);
    }

    public WebServer(String webRoot) throws IllegalArgumentException {
        this.port = 8080;
        this.webRoot = validateWebRoot(webRoot);
        this.pool = Executors.newFixedThreadPool(10);
    }

    public WebServer(int port, String webRoot) throws IllegalArgumentException {
        this.port = port;
        this.webRoot = validateWebRoot(webRoot);
        this.pool = Executors.newFixedThreadPool(10);
    }

    public WebServer(int port, int maxThreads) {
        this.port = port;
        this.webRoot = new File(".");
        this.pool = Executors.newFixedThreadPool(maxThreads);
    }

    public WebServer(int port, int maxThreads, String webRoot) throws IllegalArgumentException {
        this.port = port;
        this.webRoot = validateWebRoot(webRoot);
        this.pool = Executors.newFixedThreadPool(maxThreads);
    }

    public void start() {
        running = true;
        if (router == null)
            router = new StaticRouter();
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);
            while (running) {
                Socket socket = serverSocket.accept();
                pool.submit(new ConnectionHandler(socket, webRoot, router));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null)
                serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        pool.shutdown();
    }

    public WebServer setRouter(Router router) throws IllegalStateException {
        this.router = router;
        return this;
    }

    public Router getRouter() {
        return router;
    }

    private File validateWebRoot(String webRoot) throws IllegalArgumentException {
        File root = new File(webRoot);
        if (!root.exists() || !root.isDirectory()) {
            throw new IllegalArgumentException("Invalid web root directory: " + webRoot);
        }
        return root;
    }
}
