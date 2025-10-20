package com.hindbiswas.server.facade;

import java.util.HashMap;
import java.util.Map;

import com.hindbiswas.server.http.Request;

public class Context extends com.hindbiswas.jhp.Context {
    public Context() {
        super();
    }

    public Context(Request req) {
        super();
        injectRequest(req);
    }

    @Override
    public void add(String key, Object value) {
        if (key != null && key.startsWith("__")) {
            throw new IllegalArgumentException("Keys starting with '__' are reserved for system values.");
        }
        super.add(key, value);
    }

    public void merge(Context other) {
        if (other == null)
            return;

        for (Map.Entry<String, Object> entry : other.getContext().entrySet()) {
            super.add(entry.getKey(), entry.getValue()); // reuse add for conversion
        }
    }

    private void injectRequest(Request req) {
        if (req == null)
            return;

        Map<String, Object> server = new HashMap<>();
        server.put("method", req.method);
        server.put("path", req.path);
        server.put("version", req.version);

        super.add("__server", server);
        super.add("__header", (req.headers == null) ? new HashMap<>() : new HashMap<>(req.headers));
        super.add("__get", (req.params == null) ? new HashMap<>() : new HashMap<>(req.params));
        super.add("__post", (req.body == null ? new HashMap<>() : new HashMap<>(req.body)));
        
        Map<String, String> cookieValues = new HashMap<>();
        if (req.cookies != null) {
            req.cookies.forEach((name, cookie) -> cookieValues.put(name, cookie.getValue()));
        }
        super.add("__cookie", cookieValues);
    }

}
