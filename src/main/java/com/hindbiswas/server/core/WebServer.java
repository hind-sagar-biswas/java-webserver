package com.hindbiswas.server.core;

import com.hindbiswas.server.facade.JhpEngine;
import com.hindbiswas.server.handler.ConnectionHandler;
import com.hindbiswas.server.logger.Logger;
import com.hindbiswas.server.routing.Router;
import com.hindbiswas.server.routing.StaticRouter;
import com.hindbiswas.server.session.SessionConfig;
import com.hindbiswas.server.session.SessionManager;

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

    /** The session manager for handling sessions */
    private final SessionManager sessionManager;

    /**
     * Constructs a WebServer using default settings.
     * Port: 8080, Web root: current directory, Max threads: 10.
     */
    public WebServer() {
        this(8000);
    }

    /**
     * Constructs a WebServer with a custom port.
     *
     * @param port the port number to listen on
     */
    public WebServer(int port) {
        this(port, 10);
    }

    /**
     * Constructs a WebServer with a custom port.
     *
     * @param port the port number to listen on
     */
    public WebServer(int port, SessionConfig sessionConfig) {
        this(port, 10, ".", sessionConfig);
    }

    /**
     * Constructs a WebServer with a custom web root directory.
     *
     * @param webRoot the root directory to serve files from
     * @throws IllegalArgumentException if the directory is invalid
     */
    public WebServer(String webRoot) throws IllegalArgumentException {
        this(8000, 10, webRoot);
    }

    /**
     * Constructs a WebServer with a custom port and web root.
     *
     * @param port    the port number to listen on
     * @param webRoot the root directory to serve files from
     * @throws IllegalArgumentException if the directory is invalid
     */
    public WebServer(int port, String webRoot) throws IllegalArgumentException {
        this(port, 10, webRoot);
    }

    /**
     * Constructs a WebServer with a custom port, web root and session config.
     *
     * @param port          the port number to listen on
     * @param webRoot       the root directory to serve files from
     * @param sessionConfig the session config to use
     * @throws IllegalArgumentException if the directory is invalid
     */
    public WebServer(int port, String webRoot, SessionConfig sessionConfig) throws IllegalArgumentException {
        this(port, 10, webRoot, sessionConfig);
    }

    /**
     * Constructs a WebServer with a custom port and maximum thread count.
     *
     * @param port       the port number to listen on
     * @param maxThreads the maximum number of threads for the pool
     */
    public WebServer(int port, int maxThreads) {
        this(port, maxThreads, ".");
    }

    /**
     * Constructs a WebServer wit full configuration.
     *
     * @param port           the port number to listen on
     * @param maxThreads     the maximum number of threads for the pool
     * @param webRoot        the root directory to serve files from
     * @param sessionManager the session manager to use
     * @throws IllegalArgumentException if the directory is invalid
     */
    public WebServer(int port, int maxThreads, String webRoot, SessionConfig sessionConfig)
            throws IllegalArgumentException {
        this.port = port;
        this.webRoot = validateWebRoot(webRoot);
        this.pool = Executors.newFixedThreadPool(maxThreads);

        if (sessionConfig == null)
            sessionConfig = new SessionConfig();
        this.sessionManager = new SessionManager(sessionConfig);
    }

    /**
     * Constructs a WebServer without session manager.
     *
     * @param port       the port number to listen on
     * @param maxThreads the maximum number of threads for the pool
     * @param webRoot    the root directory to serve files from
     * @throws IllegalArgumentException if the directory is invalid
     */
    public WebServer(int port, int maxThreads, String webRoot) throws IllegalArgumentException {
        this(port, maxThreads, webRoot, null);
    }

    /**
     * Starts the web server. Initializes the router if necessary,
     * opens the server socket, and begins accepting and dispatching connections.
     */
    public void start() {
        running = true;
        if (router == null)
            router = new StaticRouter();

        // Initialize JHP engine singleton if not already initialized
        if (!JhpEngine.isInitialized()) {
            try {
                JhpEngine.initialize(this);
            } catch (IOException e) {
                Logger.err("Failed to initialize JHP engine: " + e.getMessage());
                stop();
            }
        }

        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(3000); // 3-second timeout for accept()

            Logger.log("Server started on port " + port);

            while (running) {
                try {
                    Socket socket = serverSocket.accept();
                    pool.submit(new ConnectionHandler(socket, webRoot, router, sessionManager));
                } catch (RejectedExecutionException e) {
                    Logger.err("Task rejected: " + e.getMessage());
                } catch (SocketTimeoutException ignored) {
                    // Continue checking the running flag
                }
            }
        } catch (Exception e) {
            Logger.err("Exception in server: " + e.getMessage());
        }
    }

    /**
     * Stops the web server gracefully.
     * Closes the server socket, shuts down the thread pool, and stops the session
     * manager.
     */
    public void stop() {
        running = false;
        try {
            if (serverSocket != null)
                serverSocket.close();
        } catch (IOException e) {
            Logger.err("Exception in server: " + e.getMessage());
        }

        pool.shutdown();
        try {
            if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            Logger.err("Exception in server: " + e.getMessage());
            pool.shutdownNow();
        }

        // Shutdown session manager to stop cleanup scheduler and close storage
        if (sessionManager != null) {
            sessionManager.shutdown();
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

    /**
     * returns the webroot path string
     *
     * @return the path string to validate
     */
    public String getWebRoot() throws IOException {
        return webRoot.getCanonicalPath();
    }

    /**
     * Gets the JHP template engine singleton instance.
     *
     * @return the JhpEngine instance, or null if not initialized
     */
    public static JhpEngine getEngine() {
        return JhpEngine.getInstance();
    }

    /**
     * Gets the session manager used by this server.
     *
     * @return the SessionManager instance
     */
    public SessionManager getSessionManager() {
        return sessionManager;
    }
}
