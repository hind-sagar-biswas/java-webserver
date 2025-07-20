package com.hindbiswas.server;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;

/**
 * Response
 */
public class Response {

    private int statusCode = 200;
    private String statusMessage = "OK";
    private byte[] body;
    private String mimeType = "text/html";

    public Response(int statusCode, String statusMessage, byte[] body, String mimeType) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.body = body;
        this.mimeType = mimeType;
    }

    public Response(Request request, File webRoot) {
        File resource;
        String path = (request.path.startsWith("/")) ? request.path.substring(1) : request.path;
        try {
            String decoded = URLDecoder.decode(path, "UTF-8");
            File raw = new File(webRoot, decoded);
            resource = HttpUtils.indexIfDirectory(raw.getCanonicalFile());

        } catch (UnsupportedEncodingException e) {
            this.statusCode = 500;
            this.statusMessage = "Internal Server Error";
            e.printStackTrace();
            return;
        } catch (IOException e) {
            this.statusCode = 500;
            this.statusMessage = "Internal Server Error";
            e.printStackTrace();
            return;
        }

        if (!HttpUtils.ensureResourceUnderWebRoot(resource, webRoot)) {
            this.statusCode = 403;
            this.statusMessage = "Forbidden";
        } else if (!HttpUtils.validateStaticMethod(request.method)) {
            this.statusCode = 405;
            this.statusMessage = "Method Not Allowed";
        } else if (!HttpUtils.ensureResourceExists(resource)) {
            this.statusCode = 404;
            this.statusMessage = "Not Found";
        } else {
            this.statusCode = 200;
            this.statusMessage = "OK";
            this.mimeType = HttpUtils.guessMime(resource.getName());
            try {
                this.body = Files.readAllBytes(resource.toPath());
            } catch (IOException e) {
                this.statusCode = 500;
                this.statusMessage = "Internal Server Error";
                e.printStackTrace();
            }
        }
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public String getMimeType() {
        return mimeType;
    }

    public byte[] getBody() {
        if (statusCode == 200) {
            return body;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><title>").append(statusCode).append(" ").append(statusMessage).append("</title></head>");
        sb.append("<body><h1>").append(statusCode).append(" ").append(statusMessage).append("</h1></body></html>");
        return sb.toString().getBytes();
    }

    @Override
    public String toString() {
        int length = (body != null ? body.length : 0);

        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ").append(statusCode).append(" ").append(statusMessage).append("\r\n");
        sb.append("Content-Type: ").append(mimeType).append("\r\n");
        sb.append("Content-Length: ").append(length).append("\r\n");
        sb.append("Connection: close").append("\r\n");
        return sb.toString();
    }
}
