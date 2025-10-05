package com.hindbiswas.server.routing;

import com.hindbiswas.server.http.HttpResponse;
import com.hindbiswas.server.http.Request;
import com.hindbiswas.server.http.Response;

import java.io.File;

/**
 * ApiRouter: Strict router that only handles defined API routes.
 * Returns 404 for unmatched paths.
 */
public class ApiRouter extends AbstractMethodRouter {
    @Override
    protected HttpResponse fallback(Request request, File webRoot) {
        return Response.jsonError(404).toHttpResponse();
    }
}
