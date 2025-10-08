package com.hindbiswas.server.facade;

import java.util.HashMap;
import java.util.Map;

import com.hindbiswas.server.http.Cookie;
import com.hindbiswas.server.http.Request;
import com.hindbiswas.server.session.Session;

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

        // Store request object for session utilities
        super.add("__request__", req);

        Map<String, Object> server = new HashMap<>();
        server.put("method", req.method);
        server.put("path", req.path);
        server.put("version", req.version);

        super.add("__server", server);
        super.add("__header", (req.headers == null) ? new HashMap<>() : new HashMap<>(req.headers));
        super.add("__get", (req.params == null) ? new HashMap<>() : new HashMap<>(req.params));
        super.add("__post", (req.body == null ? new HashMap<>() : new HashMap<>(req.body)));
        
        // Inject cookies
        Map<String, String> cookieMap = new HashMap<>();
        if (req.cookies != null) {
            for (Map.Entry<String, Cookie> entry : req.cookies.entrySet()) {
                cookieMap.put(entry.getKey(), entry.getValue().getValue());
            }
        }
        super.add("__cookie", cookieMap);
        
        // Inject session data
        Session session = req.getSession(false); // Don't create if doesn't exist
        Map<String, Object> sessionMap = new HashMap<>();
        if (session != null) {
            sessionMap.put("id", session.getId());
            sessionMap.put("creationTime", session.getCreationTime());
            sessionMap.put("lastAccessedTime", session.getLastAccessedTime());
            sessionMap.put("maxInactiveInterval", session.getMaxInactiveInterval());
            // Note: session attributes are not directly exposed here for security
            // Use session utility functions in JHP instead
        }
        super.add("__session", sessionMap);
    }

}
