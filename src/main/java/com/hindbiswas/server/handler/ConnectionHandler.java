package com.hindbiswas.server.handler;

import com.hindbiswas.server.http.HttpResponse;
import com.hindbiswas.server.http.HttpUtils;
import com.hindbiswas.server.http.Request;
import com.hindbiswas.server.http.Response;
import com.hindbiswas.server.routing.Router;
import com.hindbiswas.server.routing.StaticRouter;
import com.hindbiswas.server.session.SessionManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Handles an individual client connection to the server.
 * 
 * This class is responsible for reading HTTP requests from a client socket,
 * resolving the request using a router, sending the appropriate HTTP response,
 * and managing connection persistence based on HTTP headers.
 */
public class ConnectionHandler implements Runnable {

    /** The socket representing the client connection. */
    private final Socket client;

    /** The root directory from which static files will be served. */
    private final File webRoot;

    /** The router used to handle the request and determine the response. */
    private final Router router;

    /** The session manager for handling sessions */
    private final SessionManager sessionManager;

    /**
     * Constructs a ConnectionHandler using a default static file router.
     *
     * @param client  The socket for the client connection.
     * @param webRoot The root directory for serving files.
     */
    public ConnectionHandler(Socket client, File webRoot, SessionManager sessionManager) {
        this(client, webRoot, null, sessionManager);
    }

    /**
     * Constructs a ConnectionHandler with a custom router.
     * Falls back to {@link StaticRouter} if router is null.
     *
     * @param client  The socket for the client connection.
     * @param webRoot The root directory for serving files.
     * @param router  The router used to handle requests.
     */
    public ConnectionHandler(Socket client, File webRoot, Router router, SessionManager sessionManager) {
        if (router == null)
            router = new StaticRouter();
        this.client = client;
        this.webRoot = webRoot;
        this.router = router;
        this.sessionManager = sessionManager;
    }

    /**
     * Handles the lifecycle of a single client connection.
     * Reads the incoming request, resolves it to a response using the router,
     * sends the response, and decides whether to keep the connection alive.
     */
    @Override
    public void run() {
        try (InputStream in = client.getInputStream(); OutputStream out = client.getOutputStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            boolean keepAlive = true;

            client.setSoTimeout(10000); // Set read timeout to 10 seconds

            while (keepAlive) {
                HttpResponse response = null;
                Request request = null;

                try {
                    request = new Request(reader);
                    response = router.resolve(request, webRoot);

                    System.out.println("[INCOMING]: " + request);
                    System.out.println("[OUTGOING]: " + response);
                } catch (IOException e) {
                    response = Response.error(400).toHttpResponse();
                    HttpUtils.sendResponse(out, null, response);
                    break;
                }

                HttpUtils.sendResponse(out, request, response);

                // Check for Connection header to determine if the connection should be closed
                String connHeader = request.getHeader("connection");
                if ("close".equalsIgnoreCase(connHeader) || request.isHttp10()) {
                    keepAlive = false;
                }
            }
        } catch (SocketTimeoutException e) {
            System.out.println("[TIMEOUT]: " + client.getRemoteSocketAddress());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                client.close();
            } catch (IOException ignored) {
            }
        }
    }
}
