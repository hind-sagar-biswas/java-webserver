package com.hindbiswas.server.facade;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FunctionLibrary extends com.hindbiswas.jhp.engine.FunctionLibrary {
    private class RegisteredFunction {
        final Object functional;
        final boolean injectScope;
        final MissingArgPolicy missingArgPolicy;

        RegisteredFunction(Object functional, Boolean injectScope) {
            this.functional = functional;
            this.injectScope = injectScope;
            this.missingArgPolicy = missingArgumentPolicy;
        }
    }

    private static class MethodInfo {
        final Method samMethod;
        final Class<?>[] paramTypes;
        final boolean isVarArgs;

        MethodInfo(Method samMethod) {
            this.samMethod = samMethod;
            this.paramTypes = samMethod.getParameterTypes();
            this.isVarArgs = samMethod.isVarArgs();
            // make accessible just in case
            samMethod.setAccessible(true);
        }
    }

    private static class InvocationFailureException extends Exception {
        InvocationFailureException(String message) {
            super("JHP Function Invocation Failed: " + message);
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this; // cheaper
        }
    }

    // Missing-arg policy chosen: NULL (fill missing with null)
    public enum MissingArgPolicy {
        THROW, NULL, PRIMITIVE_DEFAULTS
    }

    private final Map<String, List<RegisteredFunction>> registry = new ConcurrentHashMap<>();
    private final Map<Class<?>, MethodInfo> methodInfoCache = new ConcurrentHashMap<>();
    private final MissingArgPolicy defaultMissingArgPolicy = MissingArgPolicy.NULL;
    private final MissingArgPolicy missingArgumentPolicy;

    public FunctionLibrary() {
        super();
        this.missingArgumentPolicy = defaultMissingArgPolicy;
    }

    public FunctionLibrary(MissingArgPolicy missingArgumentPolicy) {
        super();
        this.missingArgumentPolicy = missingArgumentPolicy;
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
