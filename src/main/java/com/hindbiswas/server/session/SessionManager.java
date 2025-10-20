package com.hindbiswas.server.session;

import com.hindbiswas.server.session.storage.SessionStorage;
import com.hindbiswas.server.session.storage.InMemorySessionStorage;
import com.hindbiswas.server.session.storage.FileSessionStorage;
import com.hindbiswas.server.session.storage.SQLiteSessionStorage;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.nio.file.Path;


public class SessionManager {
    private final SessionStorage storage;
    private final ScheduledExecutorService cleanupScheduler;
    private final long cleanupIntervalSeconds;
    private final int defaultMaxInactiveInterval;

    public SessionManager(StorageType storageType, Path storagePath, 
            long cleanupIntervalSeconds, int defaultMaxInactiveInterval) {
        if (cleanupIntervalSeconds <= 0) {
            throw new IllegalArgumentException("Cleanup interval must be positive");
        }

        this.cleanupIntervalSeconds = cleanupIntervalSeconds;
        this.defaultMaxInactiveInterval = defaultMaxInactiveInterval > 0 ? defaultMaxInactiveInterval : 1800; // Default 30 minutes

        // Initialize the appropriate storage
        this.storage = createStorage(storageType, storagePath);
        this.storage.initialize();

        // Setup background cleanup
        this.cleanupScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "session-cleanup");
            t.setDaemon(true);
            return t;
        });

        startCleanupTask();
    }
    
    public SessionManager() {
        this(StorageType.MEMORY, null, 120, 1800);
    }
    
    public Session createSession() {
        return createSession(defaultMaxInactiveInterval);
    }

    public synchronized Session createSession(int maxInactiveInterval) {
        Session session = new Session(maxInactiveInterval);
        storage.save(session);
        return session;
    }
    
    public synchronized Optional<Session> getSession(String id) {
        if (id == null || id.isEmpty()) {
            return Optional.empty();
        }
        
        return storage.load(id).map(session -> {
            if (session.isExpired()) {
                storage.delete(id);
                return null;
            }
            session.updateLastAccessedTime();
            storage.save(session); // Update last accessed time
            return session;
        });
    }
    
    public synchronized void invalidate(String id) {
        if (id != null && !id.isEmpty()) {
            storage.delete(id);
        }
    }
    
    public synchronized void shutdown() {
        cleanupScheduler.shutdown();
        try {
            if (!cleanupScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        storage.shutdown();
    }
    
    private SessionStorage createStorage(StorageType type, Path storagePath) {
        switch (type) {
            case FILE:
                return new FileSessionStorage(storagePath);
            case SQLITE:
                return new SQLiteSessionStorage(storagePath);
            case MEMORY:
            default:
                return new InMemorySessionStorage();
        }
    }
    
    private void startCleanupTask() {
        cleanupScheduler.scheduleAtFixedRate(
            this::cleanupExpiredSessions,
            cleanupIntervalSeconds,
            cleanupIntervalSeconds,
            TimeUnit.SECONDS
        );
    }
    
    private synchronized void cleanupExpiredSessions() {
        try {
            int cleaned = storage.cleanup();
            if (cleaned > 0) {
                System.out.println("Cleaned up " + cleaned + " expired sessions");
            }
        } catch (Exception e) {
            System.err.println("Error during session cleanup: " + e.getMessage());
        }
    }
}
