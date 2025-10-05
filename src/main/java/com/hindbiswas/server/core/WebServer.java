package com.hindbiswas.server.core;

import com.hindbiswas.server.handler.ConnectionHandler;
import com.hindbiswas.server.routing.Router;
import com.hindbiswas.server.routing.StaticRouter;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * A simple multithreaded HTTP web server that listens for client requests
 * on a specified port and serves responses using a configurable Router.
 * 
 * <p>
 * This server supports static or custom routing, and it can serve files
 * from a specified web root directory.
 * </p>
 */
public class WebServer {

    /** Port on which the server listens */
    private final int port;

    /** Thread pool for handling client connections concurrently */
    private final ExecutorService pool;

    /** The directory used as the root for serving files */
    private final File webRoot;

    /** The router used to resolve requests to responses */
    private volatile Router router = null;

    /** Indicates whether the server is currently running */
    private volatile boolean running = false;

    /** The server socket for listening to client connections */
    private ServerSocket serverSocket;

    /**
     * Constructs a WebServer using default settings.
     * Port: 8080, Web root: current directory, Max threads: 10.
     */
    public WebServer() {
        this.port = 8080;
        this.webRoot = new File(".");
        this.pool = Executors.newFixedThreadPool(10);
    }

    /**
     * Constructs a WebServer with a custom port.
     *
     * @param port the port number to listen on
     */
    public WebServer(int port) {
        this.port = port;
        this.webRoot = new File(".");
        this.pool = Executors.newFixedThreadPool(10);
    }

    /**
     * Constructs a WebServer with a custom web root directory.
     *
     * @param webRoot the root directory to serve files from
     * @throws IllegalArgumentException if the directory is invalid
     */
    public WebServer(String webRoot) throws IllegalArgumentException {
        this.port = 8080;
        this.webRoot = validateWebRoot(webRoot);
        this.pool = Executors.newFixedThreadPool(10);
    }

    /**
     * Constructs a WebServer with a custom port and web root.
     *
     * @param port    the port number to listen on
     * @param webRoot the root directory to serve files from
     * @throws IllegalArgumentException if the directory is invalid
     */
    public WebServer(int port, String webRoot) throws IllegalArgumentException {
        this.port = port;
        this.webRoot = validateWebRoot(webRoot);
        this.pool = Executors.newFixedThreadPool(10);
    }

    /**
     * Constructs a WebServer with a custom port and maximum thread count.
     *
     * @param port       the port number to listen on
     * @param maxThreads the maximum number of threads for the pool
     */
    public WebServer(int port, int maxThreads) {
        this.port = port;
        this.webRoot = new File(".");
        this.pool = Executors.newFixedThreadPool(maxThreads);
    }

    /**
     * Constructs a WebServer with full configuration.
     *
     * @param port       the port number to listen on
     * @param maxThreads the maximum number of threads for the pool
     * @param webRoot    the root directory to serve files from
     * @throws IllegalArgumentException if the directory is invalid
     */
    public WebServer(int port, int maxThreads, String webRoot) throws IllegalArgumentException {
        this.port = port;
        this.webRoot = validateWebRoot(webRoot);
        this.pool = Executors.newFixedThreadPool(maxThreads);
    }

    /**
     * Starts the web server. Initializes the router if necessary,
     * opens the server socket, and begins accepting and dispatching connections.
     */
    public void start() {
        running = true;
        if (router == null)
            router = new StaticRouter();

        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(3000); // 3-second timeout for accept()

            System.out.println("Server started on port " + port);

            while (running) {
                try {
                    Socket socket = serverSocket.accept();
                    pool.submit(new ConnectionHandler(socket, webRoot, router));
                } catch (RejectedExecutionException e) {
                    System.err.println("Task rejected: " + e.getMessage());
                } catch (SocketTimeoutException ignored) {
                    // Continue checking the running flag
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Stops the web server gracefully.
     * Closes the server socket and shuts down the thread pool.
     */
    public void stop() {
        running = false;
        try {
            if (serverSocket != null)
                serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        pool.shutdown();
        try {
            if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
        }
    }

    /**
     * Sets a custom router for handling requests.
     *
     * @param router the router to use
     * @return this WebServer instance for chaining
     * @throws IllegalStateException if the server has already started
     */
    public WebServer setRouter(Router router) throws IllegalStateException {
        if (running)
            throw new IllegalStateException("Cannot set router after server has started.");
        this.router = router;
        return this;
    }

    /**
     * Gets the current router used by the server.
     *
     * @return the router instance
     */
    public Router getRouter() {
        return router;
    }

    /**
     * Validates and converts a web root path string to a File object.
     *
     * @param webRoot the path string to validate
     * @return the corresponding File object
     * @throws IllegalArgumentException if the path is not a valid directory
     */
    private File validateWebRoot(String webRoot) throws IllegalArgumentException {
        File root = new File(webRoot);
        if (!root.exists() || !root.isDirectory()) {
            throw new IllegalArgumentException("Invalid web root directory: " + webRoot);
        }
        return root;
    }
}
