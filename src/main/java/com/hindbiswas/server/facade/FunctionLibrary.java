package com.hindbiswas.server.facade;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FunctionLibrary extends com.hindbiswas.jhp.engine.FunctionLibrary {
    private class RegisteredFunction {
        final Object function;
        final boolean injectScope;
        final MissingArgPolicy missingArgPolicy;

        RegisteredFunction(Object function, Boolean injectScope) {
            this.function = function;
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

    public FunctionLibrary register(String name, Object function) {
        return register(name, function, false);
    }

    public FunctionLibrary registerScoped(String name, Object function) {
        return register(name, function, true);
    }

    private FunctionLibrary register(String name, Object function, boolean injectScope) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Function name cannot be null/empty");
        }
        if (function == null) {
            throw new IllegalArgumentException("Function object cannot be null");
        }

        RegisteredFunction func = new RegisteredFunction(function, injectScope);
        registry.computeIfAbsent(name, k -> Collections.synchronizedList(new ArrayList<>())).add(func);

        return this;
    }

    @Override
    public Object callFunction(String name, List<Object> args, Deque<Map<String, Object>> scopes)
            throws RuntimeException {
        if (name == null)
            throw new IllegalArgumentException("Function name cannot be null");

        // First try to find the function in overload registry
        List<RegisteredFunction> funcs = registry.get(name);
        if (funcs != null && !funcs.isEmpty()) {
            List<String> diagnostic = new ArrayList<>();
            for (RegisteredFunction func : funcs) {
                try {
                    Object output = invoke(func, args, scopes);
                    return output;
                } catch (InvocationFailureException e) {
                    diagnostic.add(e.getMessage());
                } catch (RuntimeException e) {
                    throw new RuntimeException("Error invoking registered function '" + name + "': " + e.getMessage(),
                            e);
                }
            }
        }

        return super.callFunction(name, args, scopes);
    }

    private Object invoke(RegisteredFunction regFunc, List<Object> args, Deque<Map<String, Object>> scopes)
            throws InvocationFailureException {
        MethodInfo info = getMethodInfo(regFunc.function);
        return null;
    }

    private MethodInfo getMethodInfo(Object function) {
        Class<?> cls = function.getClass();
        MethodInfo cached = methodInfoCache.get(cls);
        if (cached != null)
            return cached;
        Class<?>[] interfaces = cls.getInterfaces();

        Method sam = null;
        if (interfaces.length != 0) {
            for (Class<?> ifs : interfaces) {
                Method[] methods = ifs.getMethods();
                Method candidate = null;
                int countNonDefault = 0;
                for (Method m : methods) {
                    if (m.isDefault() || java.lang.reflect.Modifier.isStatic(m.getModifiers()))
                        continue;
                    // skip methods from Object (toString, equals, hashCode)
                    if (m.getDeclaringClass() == Object.class)
                        continue;
                    countNonDefault++;
                    candidate = m;
                }
                if (countNonDefault == 1) {
                    sam = candidate;
                    break;
                }
            }
        }

        if (sam == null) {
            throw new IllegalArgumentException("Unable to find SAM method for functional argument: " + cls.getName());
        }

        MethodInfo mi = new MethodInfo(sam);
        methodInfoCache.put(cls, mi);
        return mi;
    }
}
