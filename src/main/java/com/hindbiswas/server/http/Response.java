package com.hindbiswas.server.http;

import com.hindbiswas.server.facade.Context;
import com.hindbiswas.server.facade.JhpEngine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * A unified HTTP response representation.
 */
public class Response {
    private final int statusCode;
    private final String statusMessage;
    private final byte[] body;
    private final String mimeType;
    private final Map<String, String> headers;
    private final List<Cookie> cookies;

    /**
     * Master constructor: initializes all fields and builds headers.
     */
    private Response(int statusCode, String mimeType, byte[] body, Map<String, String> extraHeaders, List<Cookie> cookies) {
        if (!HttpUtils.isStatusCodeSupported(statusCode)) {
            throw new IllegalArgumentException("Unsupported status code: " + statusCode);
        }
        this.statusCode = statusCode;
        this.statusMessage = HttpUtils.getStatusMessage(statusCode);
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
        this.cookies = cookies != null ? new ArrayList<>(cookies) : new ArrayList<>();
    }

    /** Static factory: serve a file or directory (with index.html). */
    public static Response file(File file) throws IOException {
        File resource = HttpUtils.indexIfDirectory(file.getCanonicalFile());
        byte[] data = Files.readAllBytes(resource.toPath());
        String mime = HttpUtils.guessMime(resource.getName());
        return new Response(200, mime, data, null, null);
    }

    /** Static factory: serve a file with cookies. */
    public static Response file(File file, List<Cookie> cookies) throws IOException {
        File resource = HttpUtils.indexIfDirectory(file.getCanonicalFile());
        byte[] data = Files.readAllBytes(resource.toPath());
        String mime = HttpUtils.guessMime(resource.getName());
        return new Response(200, mime, data, null, cookies);
    }

    /** Static factory: plain text response. */
    public static Response text(String text) {
        return new Response(200, "text/plain", text.getBytes(StandardCharsets.UTF_8), null, null);
    }

    /** Static factory: plain text response with cookies. */
    public static Response text(String text, List<Cookie> cookies) {
        return new Response(200, "text/plain", text.getBytes(StandardCharsets.UTF_8), null, cookies);
    }

    /** Static factory: JSON response with 200 OK. */
    public static Response json(String json) {
        return new Response(200, "application/json", json.getBytes(StandardCharsets.UTF_8), null, null);
    }

    /** Static factory: JSON response with custom status code. */
    public static Response json(String json, int code) {
        return new Response(code, "application/json", json.getBytes(StandardCharsets.UTF_8), null, null);
    }

    /** Static factory: JSON response with cookies. */
    public static Response json(String json, List<Cookie> cookies) {
        return new Response(200, "application/json", json.getBytes(StandardCharsets.UTF_8), null, cookies);
    }

    /** Static factory: JSON response with custom status code and cookies. */
    public static Response json(String json, int code, List<Cookie> cookies) {
        return new Response(code, "application/json", json.getBytes(StandardCharsets.UTF_8), null, cookies);
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
        return new Response(statusCode, "text/plain", new byte[0], extra, null);
    }

    /** Static factory: redirect with cookies. */
    public static Response redirect(String url, List<Cookie> cookies) {
        return redirect(url, 302, cookies);
    }

    /** Static factory: redirect with specific status code and cookies. */
    public static Response redirect(String url, int statusCode, List<Cookie> cookies) {
        if (statusCode != 301 && statusCode != 302 && statusCode != 303) {
            throw new IllegalArgumentException("Redirect only supports 301, 302, or 303");
        }
        Map<String, String> extra = new HashMap<>();
        extra.put("Location", url);
        return new Response(statusCode, "text/plain", new byte[0], extra, cookies);
    }

    /** Static factory: error page with HTML body. */
    public static Response error(int statusCode) {
        if (statusCode == 204 || statusCode == 304) {
            return new Response(statusCode, "text/plain", new byte[0], null, null);
        }
        String msg = HttpUtils.getStatusMessage(statusCode);
        String html = "<html><head><title>" + statusCode + " " + msg +
                "</title></head><body><h1>" + statusCode + " " + msg +
                "</h1></body></html>";
        return new Response(statusCode, "text/html", html.getBytes(StandardCharsets.UTF_8), null, null);
    }

    /** Static factory: error page with JSON body. */
    public static Response jsonError(int statusCode) {
        if (statusCode == 204 || statusCode == 304) {
            return new Response(statusCode, "text/plain", new byte[0], null, null);
        }
        String msg = HttpUtils.getStatusMessage(statusCode);
        String json = "{\"error\": \"" + msg + "\", \"code\": " + statusCode + "}";
        return new Response(statusCode, "application/json", json.getBytes(StandardCharsets.UTF_8), null, null);
    }

    /** Static factory: render a JHP file with a Context. */
    public static Response render(File file, Context context) throws IOException {
        JhpEngine engine = JhpEngine.getInstance();
        if (engine == null) {
            throw new IllegalStateException("JhpEngine not initialized. Call JhpEngine.initialize() first.");
        }
        
        try {
            // Get the canonical path and extract relative path from webroot
            String filePath = file.getCanonicalPath();
            String webRoot = engine.getWebRoot();
            
            if (!filePath.startsWith(webRoot)) {
                throw new IOException("File is not under web root");
            }
            
            // Get relative path from webroot
            String relativePath = filePath.substring(webRoot.length());
            if (relativePath.startsWith("/") || relativePath.startsWith("\\")) {
                relativePath = relativePath.substring(1);
            }
            
            // Render the JHP file
            String rendered = engine.render(relativePath, context);
            byte[] data = rendered.getBytes(StandardCharsets.UTF_8);
            return new Response(200, "text/html", data, null, null);
        } catch (Exception e) {
            // Return 500 error with the error message
            String errorMsg = (e.getMessage() != null && !e.getMessage().isEmpty()) 
                ? e.getMessage() 
                : "JHP rendering failed";
            byte[] errorBytes = errorMsg.getBytes(StandardCharsets.UTF_8);
            return new Response(500, "text/html", errorBytes, null, null);
        }
    }

    /** Static factory: render a JHP file with a Request. */
    public static Response render(File file, Request request) throws IOException {
        return render(file, new Context(request));
    }

    /** Convert to a low-level HttpResponse (for sending over socket). */
    public HttpResponse toHttpResponse() {
        return new HttpResponse(statusCode, statusMessage, body, mimeType, headers, cookies);
    }

    /**
     * Builder-style method to add a cookie to the response.
     * Returns a new Response with the cookie added.
     */
    public Response addCookie(Cookie cookie) {
        List<Cookie> newCookies = new ArrayList<>(this.cookies);
        newCookies.add(cookie);
        return new Response(statusCode, mimeType, body, new HashMap<>(headers), newCookies);
    }

    /**
     * Builder-style method to add multiple cookies to the response.
     * Returns a new Response with the cookies added.
     */
    public Response addCookies(List<Cookie> cookies) {
        List<Cookie> newCookies = new ArrayList<>(this.cookies);
        newCookies.addAll(cookies);
        return new Response(statusCode, mimeType, body, new HashMap<>(headers), newCookies);
    }
}
