package com.hindbiswas.server;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * A utility class providing helpful methods for handling HTTP logic in a custom
 * web server.
 * 
 * <p>
 * This includes:
 * <ul>
 * <li>Determining MIME types</li>
 * <li>Validating HTTP methods and status codes</li>
 * <li>Checking file access and directory traversal safety</li>
 * <li>Sending HTTP responses to clients</li>
 * </ul>
 */
public class HttpUtils {

    // Centralized MIME type map
    private static final Map<String, String> MIME_TYPES = new HashMap<>();
    // Centralized status code → reason phrase map
    private static final Map<Integer, String> REASON_PHRASES;

    static {
        Map<Integer, String> map = new LinkedHashMap<>();
        map.put(200, "OK");
        map.put(204, "No Content");
        map.put(301, "Moved Permanently");
        map.put(302, "Found");
        map.put(303, "See Other");
        map.put(400, "Bad Request");
        map.put(401, "Unauthorized");
        map.put(403, "Forbidden");
        map.put(404, "Not Found");
        map.put(500, "Internal Server Error");
        map.put(503, "Service Unavailable");
        REASON_PHRASES = Collections.unmodifiableMap(map);

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

    /**
     * Prevent instantiation of this utility class.
     */
    private HttpUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Validates whether the given HTTP method is supported.
     *
     * @param method the HTTP method (e.g., "GET", "POST")
     * @return true if supported, false otherwise
     */
    public static boolean validateMethod(String method) {
        return method != null && SUPPORTED_METHODS.contains(method.toUpperCase());
    }

    /**
     * Validates whether the given method is a static file-serving method.
     * 
     * @param method the HTTP method (e.g., "GET", "HEAD")
     * @return true if it's valid for serving static files
     */
    public static boolean validateStaticMethod(String method) {
        return method != null && STATIC_METHODS.contains(method);
    }

    /**
     * Guesses the MIME type based on a file extension.
     *
     * @param filename the file name or path
     * @return the MIME type string (e.g., "text/html")
     */
    public static String guessMime(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot >= 0 && dot < filename.length() - 1) {
            String ext = filename.substring(dot + 1).toLowerCase();
            String mime = MIME_TYPES.get(ext);
            if (mime != null) {
                return mime;
            }
        }
        return "application/octet-stream";
    }

    /**
     * Validates whether an HTTP status code is within the valid range.
     *
     * @param statusCode the status code to check
     * @return true if between 200 and 599 (inclusive), false otherwise
     */
    public static boolean ensureValidStatusCode(int statusCode) {
        return statusCode >= 200 && statusCode <= 599;
    }

    /**
     * Ensures that a requested file is under the defined web root.
     * This prevents directory traversal attacks.
     *
     * @param requested the requested file
     * @param webRoot   the server's root directory
     * @return true if safe, false otherwise
     */
    public static boolean ensureResourceUnderWebRoot(File requested, File webRoot) {
        try {
            return requested.getCanonicalPath().startsWith(webRoot.getCanonicalPath());
        } catch (IOException e) {
            System.err.println("Failed to get canonical path for " + requested + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks if the requested file exists, is readable, and is not a directory.
     *
     * @param requested the file to check
     * @return true if valid and readable
     */
    public static boolean ensureResourceExists(File requested) {
        return requested.exists() && requested.isFile() && requested.canRead();
    }

    /**
     * If the requested file is a directory, returns the index.html inside it.
     *
     * @param requested the file or directory
     * @return the original file or its index.html if it's a directory
     */
    public static File indexIfDirectory(File requested) {
        if (requested.isDirectory()) {
            return new File(requested, "index.html");
        }
        return requested;
    }

    /**
     * Sends an HTTP response to the client via the given output stream.
     * Automatically handles HEAD requests by omitting the body.
     *
     * @param out      the output stream to write to
     * @param request  the original HTTP request
     * @param response the HTTP response to send
     */
    public static void sendResponse(OutputStream out, Request request, HttpResponse response) {
        try {
            String responseHeader = response.toString();
            byte[] responseBody = response.getBody();

            PrintWriter pw = new PrintWriter(out, false, StandardCharsets.UTF_8);
            pw.write(responseHeader);
            pw.write("\r\n");
            pw.flush();

            if (request != null && !request.method.equals("HEAD") && responseBody != null) {
                out.write(responseBody);
            }

            out.flush();
        } catch (IOException e) {
            System.err.println("Failed to send response: " + e.getMessage());
        }
    }

    /**
     * Builds an Error Page
     * 
     * @param statusCode the status code
     * @return the error page body bytes
     */
    public static byte[] buildErrorPage(int statusCode) {
        String statusMessage = REASON_PHRASES.getOrDefault(statusCode, "Error");
        String html = "<html><head><title>" + statusCode + " " + statusMessage +
                "</title></head><body><h1>" + statusCode + " " + statusMessage +
                "</h1></body></html>";
        return html.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Checks if status code is supported
     * 
     * @param statusCode the status code
     * @return true if supported
     */
    public static boolean isStatusCodeSupported(int statusCode) {
        return REASON_PHRASES.containsKey(statusCode);
    }

    /**
     * Retrieve teh status message for a given status code
     * 
     * @param statusCode the status code
     * @return the status message
     */
    public static String getStatusMessage(int statusCode) {
        return REASON_PHRASES.getOrDefault(statusCode, "Error");
    }
}
