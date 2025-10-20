package com.hindbiswas.server.http;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hindbiswas.server.facade.JhpEngine;

/**
 * Represents an HTTP response to be sent to the client.
 * Handles static file serving and basic error response generation.
 */
public class HttpResponse {

    private int statusCode = 200;
    private String statusMessage = "OK";
    private byte[] body;
    private String mimeType = "text/html";
    private Map<String, String> headers = new HashMap<>();
    private List<Cookie> cookies = new ArrayList<>();

    /**
     * Creates a custom HTTP response with status, body, and MIME type.
     *
     * @param statusCode    HTTP status code (e.g. 200, 404).
     * @param statusMessage Human-readable status message.
     * @param body          Byte array body of the response.
     * @param mimeType      MIME type of the body (e.g. "text/html", "image/png").
     */
    public HttpResponse(int statusCode, String statusMessage, byte[] body, String mimeType) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.body = body;
        this.mimeType = mimeType;
    }

    /**
     * Creates a custom HTTP response with status, body, MIME type, and custom
     * headers.
     *
     * @param statusCode    HTTP status code.
     * @param statusMessage Status message.
     * @param body          Response body.
     * @param mimeType      MIME type of the response.
     * @param headers       Additional HTTP headers.
     */
    public HttpResponse(int statusCode, String statusMessage, byte[] body, String mimeType,
            Map<String, String> headers) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.body = body;
        this.mimeType = mimeType;
        this.headers = headers;
    }

    /**
     * Creates a custom HTTP response with status, body, MIME type, custom headers,
     * and cookies.
     *
     * @param statusCode    HTTP status code.
     * @param statusMessage Status message.
     * @param body          Response body.
     * @param mimeType      MIME type of the response.
     * @param headers       Additional HTTP headers.
     * @param cookies       List of cookies to set.
     */
    public HttpResponse(int statusCode, String statusMessage, byte[] body, String mimeType,
            Map<String, String> headers, List<Cookie> cookies) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.body = body;
        this.mimeType = mimeType;
        this.headers = headers;
        this.cookies = cookies != null ? cookies : new ArrayList<>();
    }

    /**
     * Constructs a response based on the HTTP request and the web root directory.
     * Serves static files, handles directories, decodes URL path, and returns
     * appropriate status codes. Automatically renders .jhp files if JHP engine is
     * initialized.
     *
     * @param request The parsed HTTP request object.
     * @param webRoot The base directory for serving files.
     */
    public HttpResponse(Request request, File webRoot) {
        File resource;
        String path = (request.path.startsWith("/")) ? request.path.substring(1) : request.path;
        try {
            String decoded = URLDecoder.decode(path, "UTF-8");
            File raw = new File(webRoot, decoded);
            resource = HttpUtils.indexIfDirectory(raw.getCanonicalFile());
        } catch (UnsupportedEncodingException e) {
            this.statusCode = 500;
            this.statusMessage = HttpUtils.getStatusMessage(this.statusCode);
            e.printStackTrace();
            return;
        } catch (IOException e) {
            this.statusCode = 500;
            this.statusMessage = HttpUtils.getStatusMessage(this.statusCode);
            e.printStackTrace();
            return;
        }

        if (!HttpUtils.ensureResourceUnderWebRoot(resource, webRoot)) {
            this.statusCode = 403;
        } else if (request.method == null || !HttpUtils.validateMethod(request.method)) {
            this.statusCode = 405;
        } else if (!HttpUtils.ensureResourceExists(resource)) {
            this.statusCode = 404;
        } else {
            this.statusCode = 200;
            this.mimeType = HttpUtils.guessMime(resource.getName());
            try {
                if (request.method.equals("HEAD")) {
                    this.body = new byte[0];
                } else {
                    // Check if this is a .jhp file
                    JhpEngine jhpEngine = JhpEngine.getInstance();
                    if ("application/x-jhp".equals(this.mimeType) && jhpEngine != null) {
                        // Render the JHP file
                        try {
                            // Calculate relative path from webroot
                            String filePath = resource.getCanonicalPath();
                            String webRootPath = webRoot.getCanonicalPath();
                            
                            if (!filePath.startsWith(webRootPath)) {
                                throw new IOException("File is not under web root");
                            }
                            
                            String relativePath = filePath.substring(webRootPath.length());
                            if (relativePath.startsWith("/") || relativePath.startsWith("\\")) {
                                relativePath = relativePath.substring(1);
                            }
                            
                            String rendered = jhpEngine.render(relativePath, request);
                            this.body = rendered.getBytes(StandardCharsets.UTF_8);
                            this.mimeType = "text/html"; // Change MIME type to HTML after rendering
                        } catch (Exception e) {
                            this.statusCode = 500;
                            String errorMsg = (e.getMessage() != null && !e.getMessage().isEmpty())
                                    ? e.getMessage()
                                    : "JHP rendering failed";
                            this.body = errorMsg.getBytes(StandardCharsets.UTF_8);
                            this.mimeType = "text/html";
                        }
                    } else {
                        this.body = Files.readAllBytes(resource.toPath());
                    }
                }
            } catch (IOException e) {
                this.statusCode = 500;
                e.printStackTrace();
            }
        }

        this.statusMessage = HttpUtils.getStatusMessage(this.statusCode);
    }

    /**
     * Returns the HTTP status code of the response.
     *
     * @return status code (e.g. 200, 404).
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Returns the status message associated with the response code.
     *
     * @return status message (e.g. "OK", "Not Found").
     */
    public String getStatusMessage() {
        return statusMessage;
    }

    /**
     * Returns the MIME type of the response body.
     *
     * @return MIME type (e.g. "text/html", "application/json").
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Returns the raw body bytes of the response.
     * If an error status is set (4xx or 5xx), returns a generated HTML error page.
     * Preserves body for successful responses and redirects.
     *
     * @return Byte array of the response body.
     */
    public byte[] getBody() {
        // Only generate error pages for actual error codes (4xx, 5xx)
        // Preserve body for 2xx (success) and 3xx (redirects)
        if (statusCode >= 400) {
            return HttpUtils.buildErrorPage(statusCode);
        }
        return body != null ? body : new byte[0];
    }

    /**
     * Adds a cookie to this response.
     * 
     * @param cookie Cookie to add
     */
    public void addCookie(Cookie cookie) {
        if (cookie != null) {
            cookies.add(cookie);
        }
    }

    /**
     * Builds the HTTP response string (headers only, not including body bytes).
     * Automatically sets Content-Type and Content-Length if not set.
     * Adds Set-Cookie headers for each cookie.
     *
     * @return HTTP response headers as a string.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ").append(statusCode).append(" ").append(statusMessage).append("\r\n");
        
        // Add Content-Type if not already present
        if (!headers.containsKey("Content-Type")) {
            sb.append("Content-Type: ").append(mimeType);
            if (mimeType.startsWith("text/")) {
                sb.append("; charset=UTF-8");
            }
            sb.append("\r\n");
        }
        
        // Add Content-Length if not already present
        if (!headers.containsKey("Content-Length")) {
            sb.append("Content-Length: ").append(getBody().length).append("\r\n");
        }
        
        // Add regular headers
        for (Map.Entry<String, String> header : headers.entrySet()) {
            sb.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");
        }
        
        // Add Set-Cookie headers (one per cookie)
        for (Cookie cookie : cookies) {
            sb.append("Set-Cookie: ").append(cookie.toString()).append("\r\n");
        }
        
        return sb.toString();
    }
}
