package com.hindbiswas.server;

/**
 * RouteHandeler: Handles a request.
 */
@FunctionalInterface
public interface RouteHandeler {

    Response handle(Request request);
}
