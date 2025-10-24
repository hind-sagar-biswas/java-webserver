package com.hindbiswas.server.facade;

import com.hindbiswas.server.logger.Logger;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
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

        RegisteredFunction(Object function, Boolean injectScope) {
            this.function = function;
            this.injectScope = injectScope;
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
        registry.computeIfAbsent(name, _ -> Collections.synchronizedList(new ArrayList<>())).add(func);

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
                    Logger.err("Error invoking function '" + name + "': " + e.getMessage());
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
        Object[] callParams;
        try {
            callParams = prepareCallParams(info, args, scopes, regFunc.injectScope);
        } catch (InvocationFailureException ife) {
            throw ife;
        }

        try {
            Object result = info.samMethod.invoke(regFunc.function, callParams);
            return result;
        } catch (IllegalAccessException | IllegalArgumentException e) {
            Logger.err("Reflection error invoking function: " + e.getMessage());
            throw new InvocationFailureException("Invocation failed due to reflection error: " + e.getMessage());
        } catch (InvocationTargetException ite) {
            Throwable cause = ite.getCause();
            Logger.err("Function threw exception: " + (cause == null ? ite.getMessage() : cause.toString()));
            throw new RuntimeException(
                    "Function threw exception: " + (cause == null ? ite.getMessage() : cause.toString()), cause);
        }
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
            sam = findSamByClassMethods(cls);
        }

        if (sam == null) {
            throw new IllegalArgumentException("Unable to find SAM method for functional argument: " + cls.getName());
        }

        MethodInfo mi = new MethodInfo(sam);
        methodInfoCache.put(cls, mi);
        return mi;
    }

    private Method findSamByClassMethods(Class<?> cls) {
        Method[] methods = cls.getMethods();
        for (Method method : methods) {
            if (method.getDeclaringClass() == Object.class || method.isSynthetic())
                continue;
            return method;
        }
        return null;
    }

    private Object[] prepareCallParams(MethodInfo mi, List<Object> args, Deque<Map<String, Object>> scopes,
            Boolean injectScope) throws InvocationFailureException {
        Class<?>[] paramTypes = mi.paramTypes;
        int declared = paramTypes.length;

        // Copy args into mutable list
        List<Object> provided = new ArrayList<>(args == null ? Collections.emptyList() : args);

        // If injection requested: find last param assignable from Deque and reserve it
        // for scopes.
        Integer injectIndex = null;
        if (injectScope) {
            for (int i = paramTypes.length - 1; i >= 0; i--) {
                Class<?> p = paramTypes[i];
                if (Deque.class.isAssignableFrom(p)) {
                    injectIndex = i;
                    break;
                }
            }
        }

        Object[] callParams = new Object[declared];

        // Handle varargs detection
        boolean isVarArgs = mi.isVarArgs;
        Class<?> varargsComponentType = isVarArgs ? paramTypes[declared - 1].getComponentType() : null;

        int argPos = 0;
        for (int i = 0; i < declared; i++) {
            if (injectIndex != null && i == injectIndex) {
                callParams[i] = scopes;
                continue;
            }

            // If this is final varargs parameter, pack remaining provided args into array
            if (isVarArgs && i == declared - 1) {
                int remaining = Math.max(0, provided.size() - argPos);
                Object varArray = Array.newInstance(varargsComponentType, remaining);
                for (int k = 0; k < remaining; k++) {
                    Object raw = (argPos + k) < provided.size() ? provided.get(argPos + k) : null;
                    Object converted = convertArgument(raw, varargsComponentType, missingArgumentPolicy, i, argPos + k);
                    Array.set(varArray, k, converted);
                }
                callParams[i] = varArray;
                argPos += remaining;
                continue;
            }

            Object rawArg = argPos < provided.size() ? provided.get(argPos) : null;

            try {
                Object converted = convertArgument(rawArg, paramTypes[i], missingArgumentPolicy, i, argPos);
                callParams[i] = converted;
            } catch (InvocationFailureException ife) {
                throw ife;
            }

            argPos++;
        }

        // If unused provided args (and not varargs), fail
        if (!isVarArgs) {
            int declaredConsumeCount = declared - (injectIndex == null ? 0 : 1);
            if (provided.size() > declaredConsumeCount) {
                throw new InvocationFailureException(
                        "Too many arguments provided. Declared parameters (excluding injected scopes): "
                                + declaredConsumeCount + ", provided: " + provided.size());
            }
        }

        return callParams;
    }

    private Object convertArgument(Object raw, Class<?> targetType, MissingArgPolicy policy, int paramIndex,
            int argIndex)
            throws InvocationFailureException {

        if (raw == null) {
            // missing or explicit null
            if (targetType.isPrimitive()) {
                // according to policy NULL: fill with null and ultimately this will be
                // incompatible with primitive,
                // so raise a error (user chose NULL policy).
                throw new InvocationFailureException("Parameter " + paramIndex + " requires primitive type "
                        + targetType.getName() + " but argument at index " + argIndex + " is null (policy=NULL).");
            }
            return null;
        }

        // quick accept if already assignable
        if (targetType.isInstance(raw))
            return raw;

        // handle boxing/unboxing for primitives
        if (targetType.isPrimitive()) {
            return convertToPrimitive(raw, targetType, paramIndex, argIndex);
        }

        // handle Number -> boxed numeric types
        if (Number.class.isAssignableFrom(targetType) && raw instanceof Number) {
            return convertNumberToBoxed((Number) raw, targetType);
        }

        // CharSequence -> String
        if (CharSequence.class.isAssignableFrom(targetType) && raw instanceof CharSequence) {
            return raw.toString();
        }

        // Convert List -> array
        if (targetType.isArray() && raw instanceof List) {
            List<?> list = (List<?>) raw;
            Class<?> component = targetType.getComponentType();
            Object arr = Array.newInstance(component, list.size());
            for (int i = 0; i < list.size(); i++) {
                Object converted = convertArgument(list.get(i), component, policy, paramIndex, argIndex);
                Array.set(arr, i, converted);
            }
            return arr;
        }

        // Convert String -> Enum
        if (targetType.isEnum() && raw instanceof CharSequence) {
            String name = raw.toString();
            @SuppressWarnings({ "unchecked", "rawtypes" })
            Class<? extends Enum> enumType = (Class<? extends Enum>) targetType;
            try {
                @SuppressWarnings({ "rawtypes", "unchecked" })
                Enum e = Enum.valueOf(enumType, name);
                return e;
            } catch (IllegalArgumentException iae) {
                Logger.dbg("Cannot convert '" + name + "' to enum " + targetType.getName());
                throw new InvocationFailureException("Cannot convert '" + name + "' to enum " + targetType.getName());
            }
        }

        // Try numeric widening for boxed numeric types
        if (raw instanceof Number && Number.class.isAssignableFrom(targetType)) {
            return convertNumberToBoxed((Number) raw, targetType);
        }

        // Last resort: try simple cast if assignable by primitive wrapper vs primitive
        // mismatch
        if (targetType.isAssignableFrom(raw.getClass())) {
            return raw;
        }

        throw new InvocationFailureException("Cannot convert argument at index " + argIndex + " of type "
                + raw.getClass().getName() + " to parameter type " + targetType.getName() + " (param " + paramIndex
                + ").");
    }

    private Object convertNumberToBoxed(Number n, Class<?> boxedTarget) throws InvocationFailureException {
        if (boxedTarget == Integer.class)
            return n.intValue();
        if (boxedTarget == Long.class)
            return n.longValue();
        if (boxedTarget == Short.class)
            return n.shortValue();
        if (boxedTarget == Byte.class)
            return n.byteValue();
        if (boxedTarget == Float.class)
            return n.floatValue();
        if (boxedTarget == Double.class)
            return n.doubleValue();
        if (boxedTarget == java.math.BigInteger.class)
            return java.math.BigInteger.valueOf(n.longValue());
        if (boxedTarget == java.math.BigDecimal.class)
            return java.math.BigDecimal.valueOf(n.doubleValue());
        throw new InvocationFailureException("Unsupported numeric target type: " + boxedTarget.getName());
    }

    private Object convertToPrimitive(Object raw, Class<?> primitiveTarget, int paramIndex, int argIndex)
            throws InvocationFailureException {
        // Accept Number -> numeric primitives
        if (raw instanceof Number) {
            Number n = (Number) raw;
            if (primitiveTarget == int.class)
                return n.intValue();
            if (primitiveTarget == long.class)
                return n.longValue();
            if (primitiveTarget == short.class)
                return n.shortValue();
            if (primitiveTarget == byte.class)
                return n.byteValue();
            if (primitiveTarget == float.class)
                return n.floatValue();
            if (primitiveTarget == double.class)
                return n.doubleValue();
        }
        // boolean primitive
        if (primitiveTarget == boolean.class) {
            if (raw instanceof Boolean)
                return raw;
            if (raw instanceof String)
                return Boolean.parseBoolean((String) raw);
        }

        // char primitive
        if (primitiveTarget == char.class) {
            if (raw instanceof Character)
                return raw;
            if (raw instanceof CharSequence) {
                String s = raw.toString();
                if (s.length() == 1)
                    return s.charAt(0);
            }
        }

        throw new InvocationFailureException("Cannot convert argument at index " + argIndex + " of type "
                + raw.getClass().getName() + " to primitive " + primitiveTarget.getName() + " (param " + paramIndex
                + ").");
    }

}
