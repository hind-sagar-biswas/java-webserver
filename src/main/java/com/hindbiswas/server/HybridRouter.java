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
    private final Map<String, RouteHandeler> putRoutes = new HashMap<>();
    private final Map<String, RouteHandeler> patchRoutes = new HashMap<>();
    private final Map<String, RouteHandeler> deleteRoutes = new HashMap<>();

    public void get(String path, RouteHandeler handeler) {
        getRoutes.put(path, handeler);
    }

    public void post(String path, RouteHandeler handeler) {
        postRoutes.put(path, handeler);
    }

    public void put(String path, RouteHandeler handeler) {
        putRoutes.put(path, handeler);
    }

    public void patch(String path, RouteHandeler handeler) {
        patchRoutes.put(path, handeler);
    }

    public void delete(String path, RouteHandeler handeler) {
        deleteRoutes.put(path, handeler);
    }

    @Override
    public HttpResponse resolve(Request request, File webRoot) {
        RouteHandeler handeler = null;
        switch (request.method) {
            case "GET" -> handeler = getRoutes.get(request.path);
            case "POST" -> handeler = postRoutes.get(request.path);
            case "PUT" -> handeler = putRoutes.get(request.path);
            case "PATCH" -> handeler = patchRoutes.get(request.path);
            case "DELETE" -> handeler = deleteRoutes.get(request.path);
        }

        if (handeler != null) {
            return handeler.handle(request).toHttpResponse();
        }

        if (!request.method.equals("GET") && !request.method.equals("POST")) {
            return Response.error(405).toHttpResponse();
        }

        return new HttpResponse(request, webRoot);
    }

}
