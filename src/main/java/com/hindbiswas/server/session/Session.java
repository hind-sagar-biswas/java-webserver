package com.hindbiswas.server.session;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import com.hindbiswas.server.util.RandomUtils;

public class Session implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String managerId;
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();
    private final long creationTime;
    private volatile long lastAccessedTime;
    private volatile int maxInactiveInterval; // in seconds

    public Session(int maxInactiveInterval, SessionManager manager) {
        this.id = RandomUtils.ulid.apply();
        this.creationTime = System.currentTimeMillis();
        this.lastAccessedTime = this.creationTime;
        this.maxInactiveInterval = maxInactiveInterval;
        this.managerId = manager.getId();
    }

    public Session(SessionManager manager) {
        this(1800, manager);
    }

    public static Session getSession(String id) {
        SessionManager manager = SessionManager.getInstance(id).orElse(null);
        if (manager == null) {
            return null;
        }
        return manager.getSession(id).orElse(null);
    }

    public String getId() {
        return id;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public long getLastAccessedTime() {
        return lastAccessedTime;
    }

    public int getMaxInactiveInterval() {
        return maxInactiveInterval;
    }

    public void setMaxInactiveInterval(int maxInactiveInterval) {
        this.maxInactiveInterval = maxInactiveInterval;
    }

    public void invalidate() {
        attributes.clear();
        this.maxInactiveInterval = 0; // Mark as expired
        SessionManager manager = SessionManager.getInstance(managerId).orElse(null);
        if (manager != null)
            manager.invalidate(this.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Session session = (Session) o;
        return id.equals(session.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public boolean isExpired() {
        if (maxInactiveInterval < 0) {
            return false; // Never expires
        }
        return (System.currentTimeMillis() - lastAccessedTime) > (maxInactiveInterval * 1000L);
    }

    public void updateLastAccessedTime() {
        this.lastAccessedTime = System.currentTimeMillis();
    }

    public Object get(String name) {
        return attributes.get(name);
    }

    public void set(String name, Object value) {
        attributes.put(name, value);
    }

    public void remove(String name) {
        attributes.remove(name);
    }

    public boolean exists(String name) {
        return attributes.containsKey(name);
    }

    /**
     * Returns a copy of the session attributes map.
     * Used by Context to create session proxy for JHP templates.
     */
    public Map<String, Object> getAttributes() {
        return new ConcurrentHashMap<>(attributes);
    }
}