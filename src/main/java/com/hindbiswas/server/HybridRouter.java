package com.hindbiswas.server;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Router
 */
public class HybridRouter implements Router {
    private final Map<String, RouteHandeler> getRoutes = new HashMap<>();
    private final Map<String, RouteHandeler> postRoutes = new HashMap<>();

    public void get(String path, RouteHandeler handeler) {
        getRoutes.put(path, handeler);
    }

    public void post(String path, RouteHandeler handeler) {
        postRoutes.put(path, handeler);
    }

    @Override
    public HttpResponse resolve(Request request, File webRoot) {
        if ("GET".equals(request.method)) {
            RouteHandeler handeler = getRoutes.get(request.path);
            if (handeler != null) {
                return handeler.handle(request).toHttpResponse();
            }
        } else if ("POST".equals(request.method)) {
            RouteHandeler handeler = postRoutes.get(request.path);
            if (handeler != null) {
                return handeler.handle(request).toHttpResponse();
            }
        }
        return new HttpResponse(request, webRoot);
    }

}
