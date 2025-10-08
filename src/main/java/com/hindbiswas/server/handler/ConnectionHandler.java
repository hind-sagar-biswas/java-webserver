package com.hindbiswas.server.handler;

import com.hindbiswas.server.http.Cookie;
import com.hindbiswas.server.http.HttpResponse;
import com.hindbiswas.server.http.HttpUtils;
import com.hindbiswas.server.http.Request;
import com.hindbiswas.server.http.Response;
import com.hindbiswas.server.routing.Router;
import com.hindbiswas.server.routing.StaticRouter;
import com.hindbiswas.server.session.Session;
import com.hindbiswas.server.session.SessionManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

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

    /** The session manager for handling sessions. */
    private final SessionManager sessionManager;

    /**
     * Constructs a ConnectionHandler using a default static file router.
     *
     * @param client  The socket for the client connection.
     * @param webRoot The root directory for serving files.
     */
    public ConnectionHandler(Socket client, File webRoot) {
        this.client = client;
        this.webRoot = webRoot;
        this.router = new StaticRouter();
        this.sessionManager = null;
    }

    /**
     * Constructs a ConnectionHandler with a custom router.
     * Falls back to {@link StaticRouter} if router is null.
     *
     * @param client  The socket for the client connection.
     * @param webRoot The root directory for serving files.
     * @param router  The router used to handle requests.
     */
    public ConnectionHandler(Socket client, File webRoot, Router router) {
        if (router == null)
            router = new StaticRouter();
        this.client = client;
        this.webRoot = webRoot;
        this.router = router;
        this.sessionManager = null;
    }

    /**
     * Constructs a ConnectionHandler with a custom router and session manager.
     * Falls back to {@link StaticRouter} if router is null.
     *
     * @param client         The socket for the client connection.
     * @param webRoot        The root directory for serving files.
     * @param router         The router used to handle requests.
     * @param sessionManager The session manager for handling sessions.
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
                Session session = null;
                boolean isNewSession = false;

                try {
                    request = new Request(reader);
                    
                    // Set session manager on request
                    if (sessionManager != null) {
                        request.setSessionManager(sessionManager);
                        session = request.getSession(); // Creates if doesn't exist
                        
                        // Check if this is a new session (no session cookie was sent)
                        String sessionCookieName = sessionManager.getConfig().getCookieName();
                        Cookie sessionCookie = request.getCookie(sessionCookieName);
                        isNewSession = (sessionCookie == null || sessionCookie.getValue() == null || sessionCookie.getValue().isEmpty());
                    }
                    
                    response = router.resolve(request, webRoot);

                    System.out.println("[INCOMING]: " + request);
                    System.out.println("[OUTGOING]: " + response);
                } catch (IOException e) {
                    response = Response.error(400).toHttpResponse();
                    HttpUtils.sendResponse(out, null, response);
                    break;
                }

                // Add session cookie if new session
                if (sessionManager != null && session != null && isNewSession) {
                    Cookie sessionCookie = sessionManager.getConfig().createSessionCookie(session.getId());
                    
                    // Add cookie
                    List<Cookie> cookies = new ArrayList<>();
                    cookies.add(sessionCookie);
                    
                    // Create new response with session cookie
                    HttpResponse newResponse = new HttpResponse(
                        response.getStatusCode(),
                        response.getStatusMessage(),
                        response.getBody(),
                        response.getMimeType(),
                        new java.util.HashMap<>(),
                        cookies
                    );
                    
                    // Use the new response with Set-Cookie header
                    response = newResponse;
                }

                HttpUtils.sendResponse(out, request, response);

                // Check for Connection header to determine if the connection should be closed
                String connHeader = request.getHeader("connection");
                if ("close".equalsIgnoreCase(connHeader) || request.isHttp10()) {
                    keepAlive = false;
                }
                
                // Save session if modified
                if (sessionManager != null && session != null) {
                    sessionManager.getSession(session.getId()); // update last accessed time
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
