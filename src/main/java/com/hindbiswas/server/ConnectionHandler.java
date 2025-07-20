package com.hindbiswas.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

/**
 * ConnectionHandler
 */
public class ConnectionHandler implements Runnable {

    private final Socket client;
    private final File webRoot;

    public ConnectionHandler(Socket client, File webRoot) {
        this.client = client;
        this.webRoot = webRoot;
    }

    @Override
    public void run() {
        try (InputStream in = client.getInputStream(); OutputStream out = client.getOutputStream();) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            Request request = new Request(reader);
            Response response = new Response(request, webRoot);

            System.out.println("[INCOMING]: " + request);
            System.out.println("[OUTGOING]: " + response);

            HttpUtils.sendResponse(out, request, response);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                client.close();
            } catch (IOException ignored) {
            }
        }
    }
}
