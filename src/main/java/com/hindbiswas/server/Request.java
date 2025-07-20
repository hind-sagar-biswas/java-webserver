package com.hindbiswas.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

/**
 * Request
 */
public class Request {

    public final String method;
    public final String path;
    public final String version;
    public final Map<String, String> headers;
    public String body;

    public Request(String method, String path, String version, Map<String, String> headers, String body) {
        this.method = method;
        this.path = path;
        this.version = version;
        this.headers = headers;
        this.body = body;
    }

    public Request(BufferedReader reader) throws IOException {
        // First line always contains the method, path, and version
        String line = reader.readLine(); // GET / HTTP/1.1
        String[] parts = line.split(" ");
        this.method = parts[0];
        this.path = parts[1];
        this.version = parts[2];

        // Parse headers
        this.headers = parseHeaders(reader);
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

    public String toString() {
        // Build a string representation of the request
        StringBuilder sb = new StringBuilder();
        sb.append(method).append(" ").append(path).append(" ").append(version).append("\n");
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        sb.append("\n");
        return sb.toString();
    }
}
