package com.hindbiswas.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Request
 */
public class Request {

    public final String method;
    public final String path;
    public final String version;
    public final Map<String, String> body;
    public final Map<String, String> params;
    public final Map<String, String> headers;
    private int contentLength;

    public Request(String method, String path, String version, Map<String, String> headers, Map<String, String> body) {
        String[] pathParts = path.split("\\?", 2);

        this.method = method.toUpperCase();
        this.path = pathParts[0];
        this.version = version;
        this.headers = headers;
        this.params = pathParts.length > 1 ? parseParams(pathParts[1]) : new HashMap<>();
        this.body = body != null ? body : new HashMap<>();
    }

    public Request(BufferedReader reader) throws IOException {
        // First line always contains the method, path, and version
        String line = reader.readLine(); // GET / HTTP/1.1
        String[] parts = line.split(" ");

        String[] pathParts = parts[1].split("\\?", 2);

        this.method = parts[0].toUpperCase();
        this.path = pathParts[0];
        this.version = parts[2];

        // Parse headers
        this.headers = parseHeaders(reader);

        // Parse query parameters
        this.params = pathParts.length > 1 ? parseParams(pathParts[1]) : new HashMap<>();

        // Parse body
        if (headers.containsKey("content-length")) {
            this.body = parseBody(reader);
        } else {
            this.body = new HashMap<>();
        }
    }

    private Map<String, String> parseHeaders(BufferedReader reader) throws IOException {
        Map<String, String> headers = new HashMap<>();
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            String[] parts = line.split(":", 2);
            if (parts.length == 2) {
                String name = parts[0].trim().toLowerCase();
                String value = parts[1].trim();

                headers.put(name, value);

                if (name.equals("content-length")) {
                    contentLength = Integer.parseInt(value);
                }
            }
        }
        return headers;
    }

    private Map<String, String> parseBody(BufferedReader reader) throws IOException {
        char[] buffer = new char[contentLength];
        int read = reader.read(buffer);
        String body = new String(buffer, 0, read);

        String contentType = headers.get("content-type");
        if (contentType == null)
            return new HashMap<>();

        if (contentType.startsWith("application/x-www-form-urlencoded")) {
            Map<String, String> formData = new HashMap<>();
            String[] pairs = body.split("&");
            for (String pair : pairs) {
                String[] kv = pair.split("=", 2);
                if (kv.length == 2) {
                    formData.put(URLDecoder.decode(kv[0], "UTF-8"), URLDecoder.decode(kv[1], "UTF-8"));
                }
            }

            return formData;
        }

        if (contentType.startsWith("application/json")) {
            Map<String, String> jsonBody = new HashMap<>();
            jsonBody.put("raw", body);
            return jsonBody;
        }

        return new HashMap<>();
    }

    private Map<String, String> parseParams(String query) {
        Map<String, String> params = new HashMap<>();
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
        for (Map.Entry<String, String> entry : body.entrySet()) {
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
