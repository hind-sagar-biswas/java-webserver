package com.hindbiswas.server.handler;

import com.hindbiswas.server.http.Request;
import com.hindbiswas.server.http.Response;

/**
 * RouteHandler: Handles a request.
 */
@FunctionalInterface
public interface RouteHandler {

    Response handle(Request request);
}
