package com.hindbiswas.server;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * HttpUtils
 */
public class HttpUtils {

    private static final Map<String, String> MIME_TYPES = new HashMap<>();
    static {
        MIME_TYPES.put("html", "text/html");
        MIME_TYPES.put("htm", "text/html");
        MIME_TYPES.put("css", "text/css");
        MIME_TYPES.put("js", "application/javascript");
        MIME_TYPES.put("json", "application/json");
        MIME_TYPES.put("png", "image/png");
        MIME_TYPES.put("jpg", "image/jpeg");
        MIME_TYPES.put("jpeg", "image/jpeg");
        MIME_TYPES.put("gif", "image/gif");
        MIME_TYPES.put("ico", "image/x-icon");
        MIME_TYPES.put("svg", "image/svg+xml");
        MIME_TYPES.put("txt", "text/plain");
        MIME_TYPES.put("webp", "image/webp");
        MIME_TYPES.put("woff", "font/woff");
        MIME_TYPES.put("woff2", "font/woff2");
        MIME_TYPES.put("ttf", "font/ttf");
        MIME_TYPES.put("mp4", "video/mp4");
        MIME_TYPES.put("pdf", "application/pdf");
    }

    private static final Set<String> SUPPORTED_METHODS = Set.of("GET", "POST", "HEAD", "PUT", "PATCH", "DELETE");
    private static final Set<String> STATIC_METHODS = Set.of("GET", "HEAD");

    private HttpUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean validateMethod(String method) {
        return method != null && SUPPORTED_METHODS.contains(method.toUpperCase());
    }

    public static boolean validateStaticMethod(String method) {
        return method != null && STATIC_METHODS.contains(method);
    }

    public static String guessMime(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot >= 0 && dot < filename.length() - 1) {
            String ext = filename.substring(dot + 1).toLowerCase();
            String mime = MIME_TYPES.get(ext);
            if (mime != null) {
                return mime;
            }
        }
        // Fallback
        return "application/octet-stream";
    }

    public static boolean ensureValidStatusCode(int statusCode) {
        return statusCode >= 200 && statusCode <= 599;
    }

    public static boolean ensureResourceUnderWebRoot(File requested, File webRoot) {
        try {
            return requested.getCanonicalPath().startsWith(webRoot.getCanonicalPath());
        } catch (IOException e) {
            System.err.println("Failed to get canonical path for " + requested + ": " + e.getMessage());
            return false;
        }
    }

    public static boolean ensureResourceExists(File requested) {
        return requested.exists() && requested.isFile() && requested.canRead();
    }

    public static File indexIfDirectory(File requested) {
        if (requested.isDirectory()) {
            File indexFile = new File(requested, "index.html");
            return indexFile;
        }
        return requested;
    }

    public static void sendResponse(OutputStream out, Request request, HttpResponse response) {
        try {
            String responseHeader = response.toString();
            byte[] responseBody = response.getBody();

            PrintWriter pw = new PrintWriter(out, false, StandardCharsets.UTF_8);
            pw.write(responseHeader);
            pw.write("\r\n");
            pw.flush();

            if (!request.method.equals("HEAD") && responseBody != null) {
                out.write(responseBody);
            }

            out.flush();
        } catch (IOException e) {
            System.err.println("Failed to send response: " + e.getMessage());
        }
    }
}
