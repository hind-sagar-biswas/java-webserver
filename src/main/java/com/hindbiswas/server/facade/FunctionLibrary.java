package com.hindbiswas.server.facade;

import java.lang.reflect.Method;

public class FunctionLibrary extends com.hindbiswas.jhp.engine.FunctionLibrary{
    // Missing-arg policy chosen: NULL (fill missing with null)
    public enum MissingArgPolicy { THROW, NULL, PRIMITIVE_DEFAULTS }

    private final MissingArgPolicy defaultMissingArgPolicy = MissingArgPolicy.NULL;

    public FunctionLibrary() {
        super();
    }

    public FunctionLibrary register(String name, Object functional) {
        return register(name, functional, false);
    }

    public FunctionLibrary registerScoped(String name, Object functional) {
        return register(name, functional, true);
    }

    private FunctionLibrary register(String name, Object functional, boolean injectScope) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Function name cannot be null/empty");
        }
        if (functional == null) {
            throw new IllegalArgumentException("Functional object cannot be null");
        }


        return this;
    }
}
