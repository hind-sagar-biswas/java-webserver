package com.hindbiswas.server.routing;

import com.hindbiswas.server.http.HttpResponse;
import com.hindbiswas.server.http.Request;
import com.hindbiswas.server.http.Response;

import java.io.File;

/**
 * HybridRouter: Tries API routes first, falls back to static files on GET/HEAD.
 */
public class HybridRouter extends AbstractMethodRouter {
    @Override
    public HttpResponse fallback(Request request, File webRoot) {
        if (request.method.equals("GET") || request.method.equals("HEAD"))
            return new HttpResponse(request, webRoot);

        return Response.error(405).toHttpResponse();
    }
}
