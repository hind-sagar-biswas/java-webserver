package com.hindbiswas.server.routing;

import com.hindbiswas.server.http.HttpResponse;
import com.hindbiswas.server.http.Request;

import java.io.File;

/**
 * StaticRouter: A router that only serves static files (no dynamic routes).
 */
public class StaticRouter implements Router {
    /**
     * Delegates request handling to the default static file responder.
     */
    @Override
    public HttpResponse resolve(Request request, File webRoot) {
        return new HttpResponse(request, webRoot);
    }
}
