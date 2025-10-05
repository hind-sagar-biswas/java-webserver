package com.hindbiswas.server;

/**
 * RouteHandler: Handles a request.
 */
@FunctionalInterface
public interface RouteHandler {

    Response handle(Request request);
}
