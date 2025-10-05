package com.hindbiswas.server.util;

/**
 * String utility functions for JHP templates.
 * Each method is a SAM (Single Abstract Method) functional interface.
 */
public class StringUtils {

    /**
     * Converts a string to uppercase.
     */
    @FunctionalInterface
    public interface Upper {
        String apply(String str);
    }

    /**
     * Converts a string to lowercase.
     */
    @FunctionalInterface
    public interface Lower {
        String apply(String str);
    }

    /**
     * Capitalizes the first letter of a string.
     */
    @FunctionalInterface
    public interface Capitalize {
        String apply(String str);
    }

    /**
     * Trims whitespace from both ends of a string.
     */
    @FunctionalInterface
    public interface Trim {
        String apply(String str);
    }

    /**
     * Returns the length of a string.
     */
    @FunctionalInterface
    public interface Length {
        int apply(String str);
    }

    /**
     * Checks if a string contains a substring.
     */
    @FunctionalInterface
    public interface Contains {
        boolean apply(String str, String substring);
    }

    /**
     * Replaces all occurrences of a target string with a replacement.
     */
    @FunctionalInterface
    public interface Replace {
        String apply(String str, String target, String replacement);
    }

    /**
     * Splits a string by a delimiter.
     */
    @FunctionalInterface
    public interface Split {
        String[] apply(String str, String delimiter);
    }

    /**
     * Returns a substring from start index to end index.
     */
    @FunctionalInterface
    public interface Substring {
        String apply(String str, int start, int end);
    }

    /**
     * Repeats a string n times.
     */
    @FunctionalInterface
    public interface Repeat {
        String apply(String str, int times);
    }

    /**
     * Reverses a string.
     */
    @FunctionalInterface
    public interface Reverse {
        String apply(String str);
    }

    /**
     * Checks if a string starts with a prefix.
     */
    @FunctionalInterface
    public interface StartsWith {
        boolean apply(String str, String prefix);
    }

    /**
     * Checks if a string ends with a suffix.
     */
    @FunctionalInterface
    public interface EndsWith {
        boolean apply(String str, String suffix);
    }

    // Implementations
    public static final Upper upper = str -> str == null ? "" : str.toUpperCase();
    public static final Lower lower = str -> str == null ? "" : str.toLowerCase();
    public static final Capitalize capitalize = str -> {
        if (str == null || str.isEmpty()) return "";
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    };
    public static final Trim trim = str -> str == null ? "" : str.trim();
    public static final Length length = str -> str == null ? 0 : str.length();
    public static final Contains contains = (str, substring) -> 
        str != null && substring != null && str.contains(substring);
    public static final Replace replace = (str, target, replacement) -> 
        str == null ? "" : str.replace(target == null ? "" : target, replacement == null ? "" : replacement);
    public static final Split split = (str, delimiter) -> 
        str == null ? new String[0] : str.split(delimiter == null ? "" : delimiter);
    public static final Substring substring = (str, start, end) -> {
        if (str == null) return "";
        int len = str.length();
        int s = Math.max(0, Math.min(start, len));
        int e = Math.max(s, Math.min(end, len));
        return str.substring(s, e);
    };
    public static final Repeat repeat = (str, times) -> {
        if (str == null || times <= 0) return "";
        return str.repeat(times);
    };
    public static final Reverse reverse = str -> {
        if (str == null) return "";
        return new StringBuilder(str).reverse().toString();
    };
    public static final StartsWith startsWith = (str, prefix) -> 
        str != null && prefix != null && str.startsWith(prefix);
    public static final EndsWith endsWith = (str, suffix) -> 
        str != null && suffix != null && str.endsWith(suffix);
}
