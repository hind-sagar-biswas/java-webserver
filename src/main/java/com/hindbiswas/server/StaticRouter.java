package com.hindbiswas.server;

import java.io.File;

/**
 * StaticRouter
 */
public class StaticRouter implements Router {
    @Override
    public HttpResponse resolve(Request request, File webRoot) {
        return new HttpResponse(request, webRoot);
    }

}
