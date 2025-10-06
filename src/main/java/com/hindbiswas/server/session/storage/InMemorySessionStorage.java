package com.hindbiswas.server.session.storage;

import com.hindbiswas.server.session.Session;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemorySessionStorage implements SessionStorage {
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    
    @Override
    public void save(Session session) {
        if (session == null) {
            throw new IllegalArgumentException("Session cannot be null");
        }
        sessions.put(session.getId(), session);
    }
    
    @Override
    public Optional<Session> load(String id) {
        return Optional.ofNullable(sessions.get(id));
    }
    
    @Override
    public void delete(String id) {
        sessions.remove(id);
    }
    
    @Override
    public int cleanup() {
        int initialSize = sessions.size();
        sessions.entrySet().removeIf(entry -> {
            if (entry.getValue().isExpired()) {
                return true;
            }
            return false;
        });
        return initialSize - sessions.size();
    }
    
    @Override
    public void initialize() {
    }
    
    @Override
    public void shutdown() {
        sessions.clear();
    }
}
