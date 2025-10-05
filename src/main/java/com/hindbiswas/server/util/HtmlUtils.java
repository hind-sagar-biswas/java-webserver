package com.hindbiswas.server.util;

/**
 * HTML utility functions for JHP templates.
 * Each method is a SAM (Single Abstract Method) functional interface.
 */
public class HtmlUtils {

    /**
     * Escapes HTML special characters.
     */
    @FunctionalInterface
    public interface Escape {
        String apply(String str);
    }

    /**
     * Strips HTML tags from a string.
     */
    @FunctionalInterface
    public interface StripTags {
        String apply(String str);
    }

    /**
     * Converts newlines to HTML <br> tags.
     */
    @FunctionalInterface
    public interface Nl2Br {
        String apply(String str);
    }

    /**
     * Truncates a string to a specified length and adds ellipsis.
     */
    @FunctionalInterface
    public interface Truncate {
        String apply(String str, int length);
    }

    /**
     * URL encodes a string.
     */
    @FunctionalInterface
    public interface UrlEncode {
        String apply(String str);
    }

    /**
     * URL decodes a string.
     */
    @FunctionalInterface
    public interface UrlDecode {
        String apply(String str);
    }

    // Implementations
    public static final Escape escape = str -> {
        if (str == null) return "";
        return str.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#x27;");
    };

    public static final StripTags stripTags = str -> {
        if (str == null) return "";
        return str.replaceAll("<[^>]*>", "");
    };

    public static final Nl2Br nl2br = str -> {
        if (str == null) return "";
        return str.replace("\r\n", "<br>")
                  .replace("\n", "<br>")
                  .replace("\r", "<br>");
    };

    public static final Truncate truncate = (str, length) -> {
        if (str == null) return "";
        if (length <= 0) return "";
        if (str.length() <= length) return str;
        return str.substring(0, Math.max(0, length - 3)) + "...";
    };

    public static final UrlEncode urlEncode = str -> {
        if (str == null) return "";
        try {
            return java.net.URLEncoder.encode(str, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            return str;
        }
    };

    public static final UrlDecode urlDecode = str -> {
        if (str == null) return "";
        try {
            return java.net.URLDecoder.decode(str, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            return str;
        }
    };
}
