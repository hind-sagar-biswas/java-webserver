package com.hindbiswas.server;

import java.io.File;

/**
 * Router
 */
public interface Router {

    public HttpResponse resolve(Request request, File webRoot);
}
