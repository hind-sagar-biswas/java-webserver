package com.hindbiswas.server;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * AbstractMethodRouter: Base class for route based routers.
 * Stores route handlers for all HTTP methods.
 */
public abstract class AbstractMethodRouter implements Router {
    protected final Map<String, Map<String, RouteHandeler>> routes = new HashMap<>();

    public AbstractMethodRouter() {
        for (String method : new String[] { "GET", "POST", "PUT", "PATCH", "DELETE" }) {
            routes.put(method, new HashMap<>());
        }
    }

    public void get(String path, RouteHandeler handler) {
        routes.get("GET").put(path, handler);
    }

    public void post(String path, RouteHandeler handler) {
        routes.get("POST").put(path, handler);
    }

    public void put(String path, RouteHandeler handler) {
        routes.get("PUT").put(path, handler);
    }

    public void patch(String path, RouteHandeler handler) {
        routes.get("PATCH").put(path, handler);
    }

    public void delete(String path, RouteHandeler handler) {
        routes.get("DELETE").put(path, handler);
    }

    @Override
    public HttpResponse resolve(Request request, File webRoot) {
        if (request.method == null || request.path == null) {
            return Response.error(400).toHttpResponse();
        }

        Map<String, RouteHandeler> methodRoutes = routes.get(request.method.toUpperCase());

        if (methodRoutes != null) {
            RouteHandeler handler = methodRoutes.get(request.path);
            if (handler != null) {
                return handler.handle(request).toHttpResponse();
            }
        }

        return fallback(request, webRoot);
    }

    /**
     * Called when no route matches. Can be overridden.
     */
    protected abstract HttpResponse fallback(Request request, File webRoot);
}
