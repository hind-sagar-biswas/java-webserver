package com.hindbiswas.server;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents an HTTP response to be sent to the client.
 * Handles static file serving and basic error response generation.
 */
class HttpResponse {

    private int statusCode = 200;
    private String statusMessage = "OK";
    private byte[] body;
    private String mimeType = "text/html";
    private Map<String, String> headers = new HashMap<>();

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
     * Constructs a response based on the HTTP request and the web root directory.
     * Serves static files, handles directories, decodes URL path, and returns
     * appropriate status codes.
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
                    this.body = Files.readAllBytes(resource.toPath());
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
     * If an error status is set (non-200), returns a generated HTML error page.
     *
     * @return Byte array of the response body.
     */
    public byte[] getBody() {
        if (statusCode == 200) {
            return body;
        }
        return HttpUtils.buildErrorPage(statusCode);
    }

    /**
     * Builds the HTTP response string (headers only, not including body bytes).
     * Automatically sets Content-Type and Content-Length if not set.
     *
     * @return HTTP response headers as a string.
     */
    @Override
    public String toString() {
        headers.putIfAbsent("Content-Type", mimeType + (mimeType.startsWith("text/") ? "; charset=UTF-8" : ""));
        headers.putIfAbsent("Content-Length", String.valueOf(getBody().length));

        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ").append(statusCode).append(" ").append(statusMessage).append("\r\n");
        for (Map.Entry<String, String> header : headers.entrySet()) {
            sb.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");
        }
        sb.append("\r\n");
        return sb.toString();
    }
}
