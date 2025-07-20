package com.hindbiswas.server;

import java.io.File;

/**
 * Router
 */
public class ApiRouter extends AbstractMethodRouter {
    @Override
    protected HttpResponse fallback(Request request, File webRoot) {
        return Response.jsonError(404).toHttpResponse();
    }

}
