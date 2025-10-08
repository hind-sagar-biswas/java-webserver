package com.hindbiswas.server.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.hindbiswas.server.session.Session;
import com.hindbiswas.server.session.SessionManager;

/**
 * Represents an HTTP request parsed from a socket input stream.
 * Supports parsing request line, headers, query parameters, and body
 * (form/json/text).
 */
public class Request {

    /** HTTP method (e.g., GET, POST) */
    public final String method;

    /** Request path (e.g., /api/data) */
    public final String path;

    /** HTTP version (e.g., HTTP/1.1) */
    public final String version;

    /** Parsed body parameters (form fields or raw data) */
    public final Map<String, String> body;

    /** Query parameters parsed from URL */
    public final Map<String, String> params;

    /** HTTP headers map (lowercased keys) */
    public final Map<String, String> headers;

    /** Parsed cookies from Cookie header */
    public final Map<String, Cookie> cookies;

    /** Content-Length if present */
    private int contentLength;

    /** Associated session (lazily loaded) */
    private Session session;

    /** Session manager for session operations */
    private SessionManager sessionManager;

    /**
     * Constructs a request manually (usually for testing).
     *
     * @param method   HTTP method (e.g., GET)
     * @param fullPath Path with optional query string
     * @param version  HTTP version (e.g., HTTP/1.1)
     * @param headers  Map of headers
     * @param body     Optional body data
     */
    public Request(String method, String fullPath, String version, Map<String, String> headers,
            Map<String, String> body) {
        String[] pathParts = parsePathAndParams(fullPath);
        this.method = method.toUpperCase();
        this.path = pathParts[0];
        this.version = version;
        this.headers = headers;
        this.params = pathParts.length > 1 ? parseParams(pathParts[1]) : new HashMap<>();
        this.body = body != null ? body : new HashMap<>();
        this.cookies = parseCookies(headers);
    }

    /**
     * Constructs a Request by parsing an incoming BufferedReader.
     * Supports GET, POST, query params, headers, and basic body formats.
     *
     * @param reader BufferedReader from socket InputStream
     * @throws IOException If request line is malformed or reading fails
     */
    public Request(BufferedReader reader) throws IOException {
        String line = reader.readLine(); // Example: GET /path HTTP/1.1
        if (line == null || line.trim().isEmpty()) {
            throw new IOException("Empty or null request line");
        }

        String[] parts = line.split(" ", 3);
        if (parts.length < 3) {
            throw new IOException("Invalid HTTP request line: " + line);
        }

        this.method = parts[0].toUpperCase();
        String[] pathParts = parsePathAndParams(parts[1]);
        this.path = pathParts[0];
        this.version = parts[2];

        this.headers = parseHeaders(reader);
        this.params = pathParts.length > 1 ? parseParams(pathParts[1]) : new HashMap<>();
        this.cookies = parseCookies(headers);

        if (headers.containsKey("content-length")) {
            try {
                contentLength = Integer.parseInt(headers.get("content-length"));
                this.body = parseBody(reader);
            } catch (NumberFormatException e) {
                throw new IOException("Invalid Content-Length value", e);
            }
        } else {
            this.body = new HashMap<>();
        }
    }

    /**
     * Parses path and query string.
     * 
     * @param path Full path, possibly including query string
     * @return Array: [0] path, [1] query (if present)
     */
    private String[] parsePathAndParams(String path) {
        return path.split("\\?", 2);
    }

    /**
     * Parses headers from input stream.
     * 
     * @param reader BufferedReader after request line
     * @return Map of lowercase header names to values
     * @throws IOException if reading headers fails
     */
    private Map<String, String> parseHeaders(BufferedReader reader) throws IOException {
        Map<String, String> headers = new HashMap<>();
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            String[] parts = line.split(":", 2);
            if (parts.length == 2) {
                String name = parts[0].trim().toLowerCase();
                String value = parts[1].trim();
                headers.put(name, value);
            }
        }
        return headers;
    }

    /**
     * Parses request body based on Content-Type.
     * Supports: application/x-www-form-urlencoded, application/json, text/plain
     *
     * @param reader BufferedReader after headers
     * @return Map of parsed body fields or raw data
     * @throws IOException if reading fails
     */
    private Map<String, String> parseBody(BufferedReader reader) throws IOException {
        // Validate content length
        if (contentLength < 0) {
            throw new IOException("Invalid Content-Length: negative value");
        }
        if (contentLength > 10 * 1024 * 1024) { // 10MB limit
            throw new IOException("Content-Length too large: " + contentLength);
        }
        
        char[] buffer = new char[contentLength];
        int totalRead = 0;
        
        // Read all bytes, handling partial reads
        while (totalRead < contentLength) {
            int read = reader.read(buffer, totalRead, contentLength - totalRead);
            if (read < 0) {
                break; // End of stream
            }
            totalRead += read;
        }
        
        if (totalRead == 0)
            return new HashMap<>();
        String body = new String(buffer, 0, totalRead);

        String contentType = headers.get("content-type");
        if (contentType == null)
            return new HashMap<>();

        if (contentType.startsWith("application/x-www-form-urlencoded")) {
            Map<String, String> formData = new HashMap<>();
            String[] pairs = body.split("&");
            for (String pair : pairs) {
                String[] kv = pair.split("=", 2);
                if (kv.length == 2) {
                    formData.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8), URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
                }
            }
            return formData;
        } else if (contentType.startsWith("application/json")) {
            Map<String, String> jsonBody = new HashMap<>();
            jsonBody.put("raw", body);
            return jsonBody;
        } else if (contentType.startsWith("text/plain")) {
            Map<String, String> plainBody = new HashMap<>();
            plainBody.put("text", body);
            return plainBody;
        }

        return new HashMap<>();
    }

    /**
     * Parses query parameters (URL-encoded) into a Map.
     * 
     * @param query URL query string (e.g., name=John&id=3)
     * @return Map of decoded query parameters
     */
    private Map<String, String> parseParams(String query) {
        Map<String, String> params = new HashMap<>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            params.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8), kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "");
        }
        return params;
    }

    /**
     * Converts request to a readable string (for debugging).
     * Includes method, path, version, headers, params, and body.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(method).append(" ").append(path).append(" ").append(version).append("\n");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
        }
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        for (Map.Entry<String, String> entry : body.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        sb.append("\n");
        return sb.toString();
    }

    /**
     * Returns a header value by key (case-sensitive).
     * 
     * @param key Header name
     * @return Value or null if not found
     */
    public String getHeader(String key) {
        return headers.get(key);
    }

    /**
     * Checks if the request version is HTTP/1.0.
     * 
     * @return true if HTTP/1.0, false otherwise
     */
    public boolean isHttp10() {
        return "HTTP/1.0".equalsIgnoreCase(version);
    }

    /**
     * Parses cookies from the Cookie header.
     * 
     * @param headers The request headers
     * @return Map of cookie name to Cookie object
     */
    private Map<String, Cookie> parseCookies(Map<String, String> headers) {
        Map<String, Cookie> cookieMap = new HashMap<>();
        String cookieHeader = headers.get("cookie");
        
        if (cookieHeader == null || cookieHeader.trim().isEmpty()) {
            return cookieMap;
        }
        
        // Cookie header format: name1=value1; name2=value2
        String[] cookiePairs = cookieHeader.split(";");
        for (String pair : cookiePairs) {
            pair = pair.trim();
            if (pair.isEmpty()) continue;
            
            int eq = pair.indexOf('=');
            if (eq > 0) {
                String name = pair.substring(0, eq).trim();
                String value = pair.substring(eq + 1).trim();
                Cookie cookie = new Cookie(name, value);
                cookieMap.put(name, cookie);
            }
        }
        
        return cookieMap;
    }

    /**
     * Gets a cookie by name.
     * 
     * @param name Cookie name
     * @return Cookie object or null if not found
     */
    public Cookie getCookie(String name) {
        return cookies.get(name);
    }

    /**
     * Gets all cookies.
     * 
     * @return Map of cookie name to Cookie object
     */
    public Map<String, Cookie> getCookies() {
        return new HashMap<>(cookies);
    }

    /**
     * Sets the session manager for this request.
     * Internal use only - called by ConnectionHandler.
     * 
     * @param sessionManager The session manager
     */
    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    /**
     * Gets the session associated with this request.
     * Creates a new session if one doesn't exist.
     * 
     * @return The session object
     */
    public Session getSession() {
        return getSession(true);
    }

    /**
     * Gets the session associated with this request.
     * 
     * @param create If true, creates a new session if one doesn't exist
     * @return The session object, or null if create is false and no session exists
     */
    public Session getSession(boolean create) {
        if (session != null) {
            return session;
        }
        
        if (sessionManager == null) {
            if (create) {
                throw new IllegalStateException("SessionManager not initialized");
            }
            return null;
        }
        
        // Try to get session ID from cookie
        String sessionCookieName = sessionManager.getConfig().getCookieName();
        Cookie sessionCookie = cookies.get(sessionCookieName);
        
        if (sessionCookie != null) {
            String sessionId = sessionCookie.getValue();
            session = sessionManager.getSession(sessionId).orElse(null);
        }
        
        // Create new session if requested and none exists
        if (session == null && create) {
            session = sessionManager.createSession();
        }
        
        return session;
    }

    /**
     * Invalidates the current session.
     */
    public void invalidateSession() {
        if (session != null) {
            if (sessionManager != null) {
                sessionManager.invalidate(session.getId());
            }
            session = null;
        }
    }
}
