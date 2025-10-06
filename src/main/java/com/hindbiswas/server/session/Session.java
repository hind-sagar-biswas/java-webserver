package com.hindbiswas.server.session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.hindbiswas.server.util.RandomUtils;

public class Session {
    private final String id;
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();
    private final long creationTime;
    private long lastAccessedTime;
    private int maxInactiveInterval = 1800; // 30 minutes

    public Session(int maxInactiveInterval) {
        this.id = RandomUtils.ulid.nextULID();
        this.creationTime = System.currentTimeMillis();
        this.lastAccessedTime = this.creationTime;
        this.maxInactiveInterval = maxInactiveInterval;
    }

    public Session() {
        this(1800);
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

    public boolean isValid() {
        return System.currentTimeMillis() - lastAccessedTime < maxInactiveInterval * 1000;
    }

    // Methods: getAttribute(), setAttribute(), invalidate()
}