package com.hindbiswas.server;

import java.io.File;

/**
 * Router
 */
public class HybridRouter extends AbstractMethodRouter {
    @Override
    public HttpResponse fallback(Request request, File webRoot) {
        if (request.method.equals("GET") || request.method.equals("POST"))
            return new HttpResponse(request, webRoot);

        return Response.error(405).toHttpResponse();

    }

}
