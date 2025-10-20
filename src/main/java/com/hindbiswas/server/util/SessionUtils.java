package com.hindbiswas.server.util;

import java.util.Deque;
import java.util.Map;

import com.hindbiswas.server.http.Request;
import com.hindbiswas.server.session.Session;

/**
 * Utility functions for session management in JHP templates.
 * These functions are designed to work with the JHP scoped function system.
 */
public class SessionUtils {

    /**
     * Gets a session attribute value.
     * Usage in JHP: {{ sessionGet('username') }}
     */
    @FunctionalInterface
    public interface SessionGet {
        Object apply(String key, Deque<Map<String, Object>> scopes);
    }

    /**
     * Sets a session attribute value.
     * Usage in JHP: {{ sessionSet('username', 'John') }}
     */
    @FunctionalInterface
    public interface SessionSet {
        void apply(String key, Object value, Deque<Map<String, Object>> scopes);
    }

    /**
     * Removes a session attribute.
     * Usage in JHP: {{ sessionRemove('username') }}
     */
    @FunctionalInterface
    public interface SessionRemove {
        void apply(String key, Deque<Map<String, Object>> scopes);
    }

    /**
     * Checks if a session attribute exists.
     * Usage in JHP: {{ sessionExists('username') }}
     */
    @FunctionalInterface
    public interface SessionExists {
        boolean apply(String key, Deque<Map<String, Object>> scopes);
    }

    /**
     * Invalidates the current session.
     * Usage in JHP: {{ sessionInvalidate() }}
     */
    @FunctionalInterface
    public interface SessionInvalidate {
        void apply(Deque<Map<String, Object>> scopes);
    }

    /**
     * Gets the session ID.
     * Usage in JHP: {{ sessionId() }}
     */
    @FunctionalInterface
    public interface SessionId {
        String apply(Deque<Map<String, Object>> scopes);
    }

    /**
     * Checks if a session is active.
     * Usage in JHP: {{ sessionActive() }}
     */
    @FunctionalInterface
    public interface SessionActive {
        boolean apply(Deque<Map<String, Object>> scopes);
    }

    // Implementation instances

    public static final SessionGet sessionGet = (key, scopes) -> {
        Request request = extractRequest(scopes);
        if (request == null)
            return null;

        Session session = request.getSession();
        if (session == null)
            return null;

        return session.get(key);
    };

    public static final SessionSet sessionSet = (key, value, scopes) -> {
        Request request = extractRequest(scopes);
        if (request == null)
            return;

        Session session = request.getSession();
        session.set(key, value);
    };

    public static final SessionRemove sessionRemove = (key, scopes) -> {
        Request request = extractRequest(scopes);
        if (request == null)
            return;

        Session session = request.getSession();
        if (session != null) {
            session.remove(key);
        }
    };

    public static final SessionExists sessionExists = (key, scopes) -> {
        Request request = extractRequest(scopes);
        if (request == null)
            return false;

        Session session = request.getSession();
        if (session == null)
            return false;

        return session.exists(key);
    };

    public static final SessionInvalidate sessionInvalidate = (scopes) -> {
        Request request = extractRequest(scopes);
        if (request == null)
            return;

        request.invalidateSession();
    };

    public static final SessionId sessionId = (scopes) -> {
        Request request = extractRequest(scopes);
        if (request == null)
            return null;

        Session session = request.getSession();
        if (session == null)
            return null;

        return session.getId();
    };

    public static final SessionActive sessionActive = (scopes) -> {
        Request request = extractRequest(scopes);
        if (request == null)
            return false;

        Session session = request.getSession();
        return session != null;
    };

    /**
     * Extracts the Request object from JHP scopes.
     * The request is stored in the __request__ variable by Context.
     */
    private static Request extractRequest(Deque<Map<String, Object>> scopes) {
        if (scopes == null || scopes.isEmpty()) {
            return null;
        }

        // Search through scopes for __request__
        for (Map<String, Object> scope : scopes) {
            Object req = scope.get("__request__");
            if (req instanceof Request) {
                return (Request) req;
            }
        }

        return null;
    }
}
