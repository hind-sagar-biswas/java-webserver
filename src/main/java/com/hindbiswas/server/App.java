package com.hindbiswas.server;

import com.hindbiswas.server.core.WebServer;
import com.hindbiswas.server.http.Response;
import com.hindbiswas.server.routing.HybridRouter;
import com.hindbiswas.server.session.SessionConfig;
import com.hindbiswas.server.session.StorageType;

/**
 * Example application demonstrating the Java WebServer.
 */
public class App {
    public static void main(String[] args) {
        WebServer server = new WebServer(8000, "/home/shinigami/www", new SessionConfig().setStorageType(StorageType.SQLITE));
        HybridRouter router = new HybridRouter();

        router.get("/time", _ -> {
            return Response.text("Current time: " + System.currentTimeMillis());
        });

        // Example 1: Get or create session
        router.get("/session", request -> {
            // Use getOrCreateSession() to create a session if it doesn't exist
            var session = request.getOrCreateSession();
            if (session == null) {
                return Response.error(500);
            }
            return Response.text("Session ID: " + session.getId());
        });

        // Example 2: Login - create session and store user data
        router.post("/login", request -> {
            String username = request.body.getOrDefault("username", "guest");
            
            var session = request.getOrCreateSession();
            if (session != null) {
                session.set("username", username);
                session.set("loginTime", System.currentTimeMillis());
            }
            
            return Response.json("{\"status\": \"logged in\", \"user\": \"" + username + "\"}");
        });

        // Example 3: Profile - check if session exists
        router.get("/profile", request -> {
            var session = request.getSession(); // Don't create, just check
            if (session == null) {
                return Response.json("{\"error\": \"Not logged in\"}", 401);
            }
            
            String username = (String) session.get("username");
            if (username == null) {
                return Response.json("{\"error\": \"No user data in session\"}", 401);
            }
            
            Long loginTime = (Long) session.get("loginTime");
            String json = String.format(
                "{\"username\": \"%s\", \"sessionId\": \"%s\", \"loginTime\": %d}",
                username, session.getId(), loginTime
            );
            
            return Response.json(json);
        });

        // Example 4: Logout - invalidate session
        router.post("/logout", request -> {
            boolean invalidated = request.invalidateSession();
            
            Response response = Response.json("{\"status\": \"logged out\"}");
            
            // Add delete cookie to response
            if (invalidated) {
                response = response.withCookie(request.getDeleteSessionCookie());
            }
            
            return response;
        });

        server.setRouter(router);
        server.start();
    }
}

