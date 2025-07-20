package com.hindbiswas.server;

import java.io.BufferedReader;
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

    public ConnectionHandler(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        try (InputStream in = client.getInputStream(); OutputStream out = client.getOutputStream();) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line = reader.readLine();

            System.out.println(">> " + line);
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
}
