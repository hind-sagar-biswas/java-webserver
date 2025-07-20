package com.hindbiswas.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * ConnectionHandler
 */
public class ConnectionHandler implements Runnable {

    private final Socket client;
    private Request request;

    public ConnectionHandler(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        try (InputStream in = client.getInputStream(); OutputStream out = client.getOutputStream();) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            request = new Request(reader);

            System.out.println(">> " + request);
            // TODO: parse headers, body, etc.

            String response = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/plain; charset=UTF-8\r\n" +
                    "Content-Length: 11\r\n" +
                    "\r\n" +
                    "Hello World";
            out.write(response.getBytes("UTF-8"));
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                client.close();
            } catch (IOException ignored) {
            }
        }
    }

    private Map<String, String> parseHeaders(BufferedReader reader) throws IOException {
        Map<String, String> headers = new HashMap<>();
        String line;

        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            String[] parts = line.split(": ");
            headers.put(parts[0], parts[1]);
        }

        return headers;
    }
}
