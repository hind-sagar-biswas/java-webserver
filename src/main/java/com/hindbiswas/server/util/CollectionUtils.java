package com.hindbiswas.server.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Collection utility functions for JHP templates.
 * Each method is a SAM (Single Abstract Method) functional interface.
 */
public class CollectionUtils {

    /**
     * Returns the size of a collection or array.
     */
    @FunctionalInterface
    public interface Size {
        int apply(Object obj);
    }

    /**
     * Checks if a collection or array is empty.
     */
    @FunctionalInterface
    public interface IsEmpty {
        boolean apply(Object obj);
    }

    /**
     * Joins elements of a collection or array into a string.
     */
    @FunctionalInterface
    public interface Join {
        String apply(Object obj, String delimiter);
    }

    /**
     * Returns the first element of a collection or array.
     */
    @FunctionalInterface
    public interface First {
        Object apply(Object obj);
    }

    /**
     * Returns the last element of a collection or array.
     */
    @FunctionalInterface
    public interface Last {
        Object apply(Object obj);
    }

    /**
     * Reverses a collection or array.
     */
    @FunctionalInterface
    public interface Reverse {
        List<Object> apply(Object obj);
    }

    // Implementations
    public static final Size size = obj -> {
        if (obj == null) return 0;
        if (obj instanceof Collection) return ((Collection<?>) obj).size();
        if (obj.getClass().isArray()) return java.lang.reflect.Array.getLength(obj);
        return 0;
    };

    public static final IsEmpty isEmpty = obj -> {
        if (obj == null) return true;
        if (obj instanceof Collection) return ((Collection<?>) obj).isEmpty();
        if (obj.getClass().isArray()) return java.lang.reflect.Array.getLength(obj) == 0;
        return true;
    };

    public static final Join join = (obj, delimiter) -> {
        if (obj == null) return "";
        String delim = delimiter == null ? "," : delimiter;
        
        if (obj instanceof Collection) {
            return String.join(delim, ((Collection<?>) obj).stream()
                .map(o -> o == null ? "" : o.toString())
                .toArray(String[]::new));
        }
        
        if (obj.getClass().isArray()) {
            int length = java.lang.reflect.Array.getLength(obj);
            String[] strArray = new String[length];
            for (int i = 0; i < length; i++) {
                Object element = java.lang.reflect.Array.get(obj, i);
                strArray[i] = element == null ? "" : element.toString();
            }
            return String.join(delim, strArray);
        }
        
        return obj.toString();
    };

    public static final First first = obj -> {
        if (obj == null) return null;
        
        if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            return list.isEmpty() ? null : list.get(0);
        }
        
        if (obj instanceof Collection) {
            Collection<?> coll = (Collection<?>) obj;
            return coll.isEmpty() ? null : coll.iterator().next();
        }
        
        if (obj.getClass().isArray()) {
            int length = java.lang.reflect.Array.getLength(obj);
            return length == 0 ? null : java.lang.reflect.Array.get(obj, 0);
        }
        
        return null;
    };

    public static final Last last = obj -> {
        if (obj == null) return null;
        
        if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            return list.isEmpty() ? null : list.get(list.size() - 1);
        }
        
        if (obj instanceof Collection) {
            Collection<?> coll = (Collection<?>) obj;
            if (coll.isEmpty()) return null;
            Object lastElement = null;
            for (Object element : coll) {
                lastElement = element;
            }
            return lastElement;
        }
        
        if (obj.getClass().isArray()) {
            int length = java.lang.reflect.Array.getLength(obj);
            return length == 0 ? null : java.lang.reflect.Array.get(obj, length - 1);
        }
        
        return null;
    };

    public static final Reverse reverse = obj -> {
        if (obj == null) return new ArrayList<>();
        
        List<Object> result = new ArrayList<>();
        
        if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            for (int i = list.size() - 1; i >= 0; i--) {
                result.add(list.get(i));
            }
            return result;
        }
        
        if (obj instanceof Collection) {
            List<Object> temp = new ArrayList<>((Collection<?>) obj);
            for (int i = temp.size() - 1; i >= 0; i--) {
                result.add(temp.get(i));
            }
            return result;
        }
        
        if (obj.getClass().isArray()) {
            int length = java.lang.reflect.Array.getLength(obj);
            for (int i = length - 1; i >= 0; i--) {
                result.add(java.lang.reflect.Array.get(obj, i));
            }
            return result;
        }
        
        return result;
    };
}
