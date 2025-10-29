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
        Map<String, Object> sessionProxy = extractSessionProxy(scopes);
        if (sessionProxy == null)
            return null;

        @SuppressWarnings("unchecked")
        Map<String, Object> attributes = (Map<String, Object>) sessionProxy.get("_attributes");
        if (attributes == null)
            return null;

        return attributes.get(key);
    };

    public static final SessionSet sessionSet = (key, value, scopes) -> {
        Map<String, Object> sessionProxy = extractSessionProxy(scopes);
        if (sessionProxy == null)
            return;

        String sessId = sessionProxy.get("__id") == null ? null : sessionProxy.get("__id").toString();
        if (sessId == null)
            return;

        Session session = Session.getSession(sessId);
        if (session != null) {
            session.set(key, value);
        }
    };

    public static final SessionRemove sessionRemove = (key, scopes) -> {
        Map<String, Object> sessionProxy = extractSessionProxy(scopes);
        if (sessionProxy == null)
            return;

        String sessId = sessionProxy.get("__id") == null ? null : sessionProxy.get("__id").toString();
        if (sessId == null)
            return;

        Session session = Session.getSession(sessId);
        if (session != null) {
            session.remove(key);
        }
    };

    public static final SessionExists sessionExists = (key, scopes) -> {
        Map<String, Object> sessionProxy = extractSessionProxy(scopes);
        if (sessionProxy == null)
            return false;

        @SuppressWarnings("unchecked")
        Map<String, Object> attributes = (Map<String, Object>) sessionProxy.get("_attributes");
        if (attributes == null)
            return false;

        return attributes.containsKey(key);
    };

    public static final SessionInvalidate sessionInvalidate = (scopes) -> {
        Map<String, Object> sessionProxy = extractSessionProxy(scopes);
        if (sessionProxy == null)
            return;

        String sessId = sessionProxy.get("__id") == null ? null : sessionProxy.get("__id").toString();
        Session session = Session.getSession(sessId);
        if (session != null) {
            session.invalidate();
        }
    };

    public static final SessionId sessionId = (scopes) -> {
        Map<String, Object> sessionProxy = extractSessionProxy(scopes);
        if (sessionProxy == null)
            return null;

        return (String) sessionProxy.get("_id");
    };

    public static final SessionActive sessionActive = (scopes) -> {
        Map<String, Object> sessionProxy = extractSessionProxy(scopes);
        return sessionProxy != null;
    };

    /**
     * Extracts the session proxy Map from JHP scopes.
     * The session proxy contains: _id, _attributes, _request
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> extractSessionProxy(Deque<Map<String, Object>> scopes) {
        if (scopes == null || scopes.isEmpty()) {
            return null;
        }

        // Search through scopes for __session
        for (Map<String, Object> scope : scopes) {
            Object sess = scope.get("__session");
            if (sess instanceof Map) {
                return (Map<String, Object>) sess;
            }
        }

        return null;
    }
}
