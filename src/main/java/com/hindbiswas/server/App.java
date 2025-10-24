package com.hindbiswas.server;

import com.hindbiswas.server.core.WebServer;
import com.hindbiswas.server.http.Response;
import com.hindbiswas.server.logger.Logger;
import com.hindbiswas.server.routing.HybridRouter;
import com.hindbiswas.server.session.Session;
import com.hindbiswas.server.session.SessionConfig;
import com.hindbiswas.server.session.StorageType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Complete Todo Application with Authentication
 * Features: Register, Login, Logout, Todo CRUD
 */
public class App {
    // In-memory user storage (username -> password hash)
    private static final Map<String, String> users = new ConcurrentHashMap<>();

    // In-memory todo storage (username -> list of todos)
    private static final Map<String, List<Todo>> userTodos = new ConcurrentHashMap<>();

    // Todo ID counter
    private static final AtomicInteger todoIdCounter = new AtomicInteger(1);

    static {
        // Add demo user
        users.put("demo", hashPassword("demo123"));
    }

    public static void main(String[] args) {
        Logger.initialize("./logs", true, true);
        
        SessionConfig sessionConfig = new SessionConfig()
                .setStorageType(StorageType.SQLITE)
                .setDefaultMaxInactiveInterval(3600) // 1 hour
                .setCleanupIntervalSeconds(300); // 5 minutes

        WebServer server = new WebServer(8000, "./example", sessionConfig);
        HybridRouter router = new HybridRouter();

        // ==================== Authentication Routes ====================

        // Register endpoint - Laravel style with redirects
        router.post("/register", request -> {
            String username = request.body.get("username");
            String password = request.body.get("password");
            String confirmPassword = request.body.get("confirmPassword");

            var session = request.getOrCreateSession();

            // Validation
            if (username == null || username.trim().isEmpty()) {
                if (session != null) {
                    session.set("error", "Username is required");
                }
                return Response.redirect("/register.jhp");
            }
            if (password == null || password.length() < 6) {
                if (session != null) {
                    session.set("error", "Password must be at least 6 characters");
                }
                return Response.redirect("/register.jhp");
            }
            if (!password.equals(confirmPassword)) {
                if (session != null) {
                    session.set("error", "Passwords do not match");
                }
                return Response.redirect("/register.jhp");
            }

            // Check if user already exists
            if (users.containsKey(username)) {
                if (session != null) {
                    session.set("error", "Username already exists");
                }
                return Response.redirect("/register.jhp");
            }

            // Create user
            users.put(username, hashPassword(password));
            userTodos.put(username, new ArrayList<>());

            // Auto-login after registration
            if (session != null) {
                session.set("loggedIn", true);
                session.set("username", username);
                session.set("success", "Registration successful! Welcome, " + username);
            }

            return Response.redirect("/dashboard.jhp");
        });

        // Login endpoint - Laravel style with redirects
        router.post("/login", request -> {

            String username = request.body.get("username");
            String password = request.body.get("password");

            var session = request.getOrCreateSession();

            // Validation
            if (username == null || password == null) {
                if (session != null) {
                    session.set("error", "Username and password are required");
                }
                return Response.redirect("/login.jhp");
            }

            // Check credentials
            String storedHash = users.get(username);
            if (storedHash == null || !storedHash.equals(hashPassword(password))) {
                if (session != null) {
                    session.set("error", "Invalid username or password");
                }
                return Response.redirect("/login.jhp");
            }

            // Create session
            if (session != null) {
                session.set("loggedIn", true);
                session.set("username", username);
                session.set("success", "Welcome back, " + username + "!");
            }

            return Response.redirect("/dashboard.jhp");
        });

        // Logout endpoint - Laravel style with redirect
        router.post("/logout", request -> {
            boolean invalidated = request.invalidateSession();

            Response response = Response.redirect("/login.jhp");

            if (invalidated) {
                response = response.withCookie(request.getDeleteSessionCookie());
            }

            return response;
        });

        // Also support GET logout for convenience
        router.get("/logout", request -> {
            boolean invalidated = request.invalidateSession();

            Response response = Response.redirect("/login.jhp");

            if (invalidated) {
                response = response.withCookie(request.getDeleteSessionCookie());
            }

            return response;
        });

        // ==================== Todo CRUD Routes ====================

        // Get all todos for logged-in user
        router.get("/api/todos", request -> {
            String username = getLoggedInUser(request);
            if (username == null) {
                return Response.json("{\"error\": \"Not authenticated\"}", 401);
            }

            List<Todo> todos = userTodos.getOrDefault(username, new ArrayList<>());
            return Response.json(todosToJson(todos));
        });

        // Create new todo
        router.post("/api/todos", request -> {
            String username = getLoggedInUser(request);
            if (username == null) {
                return Response.json("{\"error\": \"Not authenticated\"}", 401);
            }

            String title = request.body.get("title");
            if (title == null || title.trim().isEmpty()) {
                return Response.json("{\"error\": \"Title is required\"}", 400);
            }

            Todo todo = new Todo(todoIdCounter.getAndIncrement(), title.trim(), false);

            userTodos.computeIfAbsent(username, _ -> new ArrayList<>()).add(todo);

            return Response.json(todoToJson(todo));
        });

        // Toggle todo completion
        router.put("/api/todos", request -> {
            String username = getLoggedInUser(request);
            if (username == null) {
                return Response.json("{\"error\": \"Not authenticated\"}", 401);
            }

            String idStr = request.body.get("id");
            if (idStr == null) {
                return Response.json("{\"error\": \"Todo ID is required\"}", 400);
            }

            try {
                int id = Integer.parseInt(idStr);
                List<Todo> todos = userTodos.get(username);

                if (todos != null) {
                    for (Todo todo : todos) {
                        if (todo.id == id) {
                            todo.completed = !todo.completed;
                            return Response.json(todoToJson(todo));
                        }
                    }
                }

                return Response.json("{\"error\": \"Todo not found\"}", 404);
            } catch (NumberFormatException e) {
                return Response.json("{\"error\": \"Invalid todo ID\"}", 400);
            }
        });

        // Delete todo
        router.delete("/api/todos", request -> {
            String username = getLoggedInUser(request);
            if (username == null) {
                return Response.json("{\"error\": \"Not authenticated\"}", 401);
            }

            String idStr = request.body.get("id");
            if (idStr == null) {
                return Response.json("{\"error\": \"Todo ID is required\"}", 400);
            }

            try {
                int id = Integer.parseInt(idStr);
                List<Todo> todos = userTodos.get(username);

                if (todos != null) {
                    boolean removed = todos.removeIf(todo -> todo.id == id);
                    if (removed) {
                        return Response.json("{\"success\": true, \"message\": \"Todo deleted\"}");
                    }
                }

                return Response.json("{\"error\": \"Todo not found\"}", 404);
            } catch (NumberFormatException e) {
                return Response.json("{\"error\": \"Invalid todo ID\"}", 400);
            }
        });

        server.setRouter(router);
        Logger.log("Demo credentials: username=demo, password=demo123");
        server.start();
    }

    // ==================== Helper Methods ====================

    /**
     * Get the logged-in username from session
     */
    private static String getLoggedInUser(com.hindbiswas.server.http.Request request) {
        Session session = request.getSession();
        if (session == null) {
            return null;
        }

        Boolean loggedIn = (Boolean) session.get("loggedIn");
        if (loggedIn == null || !loggedIn) {
            return null;
        }

        return (String) session.get("username");
    }

    /**
     * Simple password hashing (use BCrypt in production!)
     */
    private static String hashPassword(String password) {
        // This is a simple hash for demo purposes
        // In production, use BCrypt, Argon2, or PBKDF2
        return Integer.toString(password.hashCode());
    }

    /**
     * Convert todo to JSON string
     */
    private static String todoToJson(Todo todo) {
        return String.format(
                "{\"id\": %d, \"title\": \"%s\", \"completed\": %b}",
                todo.id, escapeJson(todo.title), todo.completed);
    }

    /**
     * Convert list of todos to JSON array
     */
    private static String todosToJson(List<Todo> todos) {
        if (todos.isEmpty()) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < todos.size(); i++) {
            if (i > 0)
                sb.append(",");
            sb.append(todoToJson(todos.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Escape JSON special characters
     */
    private static String escapeJson(String str) {
        if (str == null)
            return "";
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // ==================== Todo Model ====================

    static class Todo {
        int id;
        String title;
        boolean completed;

        Todo(int id, String title, boolean completed) {
            this.id = id;
            this.title = title;
            this.completed = completed;
        }
    }
}
