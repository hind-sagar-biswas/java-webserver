package com.hindbiswas.server;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashMap;

/**
 * A unified HTTP response representation.
 */
public class Response {
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
    }

    private final int statusCode;
    private final String statusMessage;
    private final byte[] body;
    private final String mimeType;
    private final Map<String, String> headers;

    /**
     * Master constructor: initializes all fields and builds headers.
     */
    private Response(int statusCode, String mimeType, byte[] body, Map<String, String> extraHeaders) {
        if (!REASON_PHRASES.containsKey(statusCode)) {
            throw new IllegalArgumentException("Unsupported status code: " + statusCode);
        }
        this.statusCode = statusCode;
        this.statusMessage = REASON_PHRASES.get(statusCode);
        this.mimeType = mimeType;

        // Handle status codes that must not have a body
        if (body == null || statusCode == 204 || statusCode == 304) {
            this.body = new byte[0];
        } else {
            this.body = body;
        }

        // Build immutable headers map
        Map<String, String> hdrs = new LinkedHashMap<>();
        // Add any custom headers (e.g. Location for redirects)
        if (extraHeaders != null) {
            extraHeaders.forEach((k, v) -> hdrs.put(k.trim(), v.trim()));
        }
        this.headers = Collections.unmodifiableMap(hdrs);
    }

    /** Static factory: serve a file or directory (with index.html). */
    public static Response file(File file) throws IOException {
        File resource = HttpUtils.indexIfDirectory(file.getCanonicalFile());
        byte[] data = Files.readAllBytes(resource.toPath());
        String mime = HttpUtils.guessMime(resource.getName());
        return new Response(200, mime, data, null);
    }

    /** Static factory: plain text response. */
    public static Response text(String text) {
        return new Response(200, "text/plain", text.getBytes(StandardCharsets.UTF_8), null);
    }

    /** Static factory: JSON response with 200 OK. */
    public static Response json(String json) {
        return new Response(200, "application/json", json.getBytes(StandardCharsets.UTF_8), null);
    }

    /** Static factory: JSON response with custom status code. */
    public static Response json(String json, int code) {
        return new Response(code, "application/json", json.getBytes(StandardCharsets.UTF_8), null);
    }

    /** Static factory: redirect (302 Found by default). */
    public static Response redirect(String url) {
        return redirect(url, 302);
    }

    /** Static factory: redirect with specific 301,302, or 303. */
    public static Response redirect(String url, int statusCode) {
        if (statusCode != 301 && statusCode != 302 && statusCode != 303) {
            throw new IllegalArgumentException("Redirect only supports 301, 302, or 303");
        }
        Map<String, String> extra = new HashMap<>();
        extra.put("Location", url);
        return new Response(statusCode, "text/plain", new byte[0], extra);
    }

    /** Static factory: error page with HTML body. */
    public static Response error(int statusCode) {
        if (statusCode == 204 || statusCode == 304) {
            return new Response(statusCode, "text/plain", new byte[0], null);
        }
        String msg = REASON_PHRASES.getOrDefault(statusCode, "Error");
        String html = "<html><head><title>" + statusCode + " " + msg +
                "</title></head><body><h1>" + statusCode + " " + msg +
                "</h1></body></html>";
        return new Response(statusCode, "text/html", html.getBytes(StandardCharsets.UTF_8), null);
    }

    /** Static factory: error page with HTML body. */
    public static Response jsonError(int statusCode) {
        if (statusCode == 204 || statusCode == 304) {
            return new Response(statusCode, "text/plain", new byte[0], null);
        }
        String msg = REASON_PHRASES.getOrDefault(statusCode, "Error");
        String json = "{\"error\": \"" + msg + "\", \"code\": " + statusCode + "}";
        return new Response(statusCode, "application/json", json.getBytes(StandardCharsets.UTF_8), null);
    }

    /** Convert to a low-level HttpResponse (for sending over socket). */
    public HttpResponse toHttpResponse() {
        return new HttpResponse(statusCode, statusMessage, body, mimeType, headers);
    }
}
