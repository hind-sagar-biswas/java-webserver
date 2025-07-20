package com.hindbiswas.server;

import java.io.File;

/**
 * Router interface for dispatching HTTP requests to the appropriate handler or
 * static file.
 */
public interface Router {
    /**
     * Resolve an incoming request into an HttpResponse.
     * 
     * @param request the parsed HTTP request
     * @param webRoot the root directory for static file serving
     * @return the HttpResponse to send back
     */
    public HttpResponse resolve(Request request, File webRoot);
}
