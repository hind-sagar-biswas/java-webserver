package com.hindbiswas.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * ConnectionHandler
 */
public class ConnectionHandler implements Runnable {

    private final Socket client;
    private final File webRoot;
    private final Router router;

    public ConnectionHandler(Socket client, File webRoot) {
        this.client = client;
        this.webRoot = webRoot;
        this.router = new StaticRouter();
    }

    public ConnectionHandler(Socket client, File webRoot, Router router) {
        this.client = client;
        this.webRoot = webRoot;
        this.router = router;
    }

    @Override
    public void run() {
        try (InputStream in = client.getInputStream(); OutputStream out = client.getOutputStream();) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            boolean keepAlive = true;

            client.setSoTimeout(10000);
            while (keepAlive) {
                Request request = new Request(reader);
                HttpResponse response = router.resolve(request, webRoot);

                System.out.println("[INCOMING]: " + request);
                System.out.println("[OUTGOING]: " + response);

                HttpUtils.sendResponse(out, request, response);

                String connHeader = request.getHeader("Connection");
                if ("close".equalsIgnoreCase(connHeader) || request.isHttp10()) {
                    keepAlive = false;
                }
            }
        } catch (SocketTimeoutException e) {
            System.out.println("[TIMEOUT]: " + client.getRemoteSocketAddress());
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
