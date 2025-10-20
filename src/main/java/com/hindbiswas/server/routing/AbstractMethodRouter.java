package com.hindbiswas.server.routing;

import com.hindbiswas.server.handler.RouteHandler;
import com.hindbiswas.server.http.HttpResponse;
import com.hindbiswas.server.http.Request;
import com.hindbiswas.server.http.Response;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * AbstractMethodRouter: Base class for route based routers.
 * Stores route handlers for all HTTP methods.
 */
public abstract class AbstractMethodRouter implements Router {
    protected final Map<String, Map<String, RouteHandler>> routes = new HashMap<>();

    public AbstractMethodRouter() {
        for (String method : new String[] { "GET", "POST", "PUT", "PATCH", "DELETE" }) {
            routes.put(method, new HashMap<>());
        }
    }

    public void get(String path, RouteHandler handler) {
        routes.get("GET").put(path, handler);
    }

    public void post(String path, RouteHandler handler) {
        routes.get("POST").put(path, handler);
    }

    public void put(String path, RouteHandler handler) {
        routes.get("PUT").put(path, handler);
    }

    public void patch(String path, RouteHandler handler) {
        routes.get("PATCH").put(path, handler);
    }

    public void delete(String path, RouteHandler handler) {
        routes.get("DELETE").put(path, handler);
    }

    @Override
    public HttpResponse resolve(Request request, File webRoot) {
        if (request.method == null || request.path == null) {
            return Response.error(400).toHttpResponse();
        }

        Map<String, RouteHandler> methodRoutes = routes.get(request.method.toUpperCase());

        if (methodRoutes != null) {
            RouteHandler handler = methodRoutes.get(request.path);
            if (handler != null) {
                try {
                    return handler.handle(request).toHttpResponse();
                } catch (Exception e) {
                    return Response.error(500).toHttpResponse();
                }
            }
        }
        return fallback(request, webRoot);
    }

    /**
     * Called when no route matches. Can be overridden.
     */
    protected abstract HttpResponse fallback(Request request, File webRoot);
}
