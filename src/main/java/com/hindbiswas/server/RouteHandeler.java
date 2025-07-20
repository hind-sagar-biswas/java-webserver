package com.hindbiswas.server;

/**
 * RouteHandeler
 */
@FunctionalInterface
public interface RouteHandeler {

    Response handle(Request request);
}
