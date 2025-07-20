package com.hindbiswas.server;

import java.io.File;

/**
 * HybridRouter: Tries API routes first, falls back to static files on GET/POST.
 */
public class HybridRouter extends AbstractMethodRouter {
    @Override
    public HttpResponse fallback(Request request, File webRoot) {
        if (request.method.equals("GET") || request.method.equals("POST"))
            return new HttpResponse(request, webRoot);

        return Response.error(405).toHttpResponse();

    }

}
