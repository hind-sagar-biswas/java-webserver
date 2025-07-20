package com.hindbiswas.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

/**
 * Request
 */
public class Request {

    public final String method;
    public final String path;
    public final String version;
    public final Map<String, String> params;
    public final Map<String, String> headers;
    public String body;

    public Request(String method, String path, String version, Map<String, String> headers, String body) {
        String[] pathParts = path.split("\\?", 2);

        this.method = method;
        this.path = pathParts[0];
        this.version = version;
        this.headers = headers;
        this.params = pathParts.length > 1 ? parseParams(pathParts[1]) : new java.util.HashMap<>();
        this.body = body;
    }

    public Request(BufferedReader reader) throws IOException {
        // First line always contains the method, path, and version
        String line = reader.readLine(); // GET / HTTP/1.1
        String[] parts = line.split(" ");

        String[] pathParts = parts[1].split("\\?", 2);

        this.method = parts[0];
        this.path = pathParts[0];
        this.version = parts[2];

        // Parse headers
        this.headers = parseHeaders(reader);

        // Parse query parameters
        this.params = pathParts.length > 1 ? parseParams(pathParts[1]) : new java.util.HashMap<>();
    }

    private Map<String, String> parseHeaders(BufferedReader reader) throws IOException {
        Map<String, String> headers = new java.util.HashMap<>();
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            String[] kv = line.split(":", 2);
            headers.put(kv[0].trim(), kv[1].trim());
        }
        return headers;
    }

    private Map<String, String> parseParams(String query) {
        Map<String, String> params = new java.util.HashMap<>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            try {
                params.put(URLDecoder.decode(kv[0], "UTF-8"), kv.length > 1 ? URLDecoder.decode(kv[1], "UTF-8") : "");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return params;
    }

    public String toString() {
        // Build a string representation of the request
        StringBuilder sb = new StringBuilder();
        sb.append(method).append(" ").append(path).append(" ").append(version).append("\n");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
        }
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        sb.append("\n");
        return sb.toString();
    }

    public String getHeader(String key) {
        return headers.get(key);
    }

    public boolean isHttp10() {
        return "HTTP/1.0".equalsIgnoreCase(version);
    }
}
