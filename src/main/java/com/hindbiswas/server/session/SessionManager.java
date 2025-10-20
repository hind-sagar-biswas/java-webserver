package com.hindbiswas.server.session;

import com.hindbiswas.server.http.Cookie;
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
    private final SessionConfig config;

    /**
     * Constructs a SessionManager with a SessionConfig.
     * 
     * @param config The session configuration
     */
    public SessionManager(SessionConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("SessionConfig cannot be null");
        }
        if (config.getCleanupIntervalSeconds() <= 0) {
            throw new IllegalArgumentException("Cleanup interval must be positive");
        }

        this.config = config;

        // Initialize the appropriate storage
        this.storage = createStorage(config.getStorageType(), config.getStoragePath());
        this.storage.initialize();

        // Setup background cleanup
        this.cleanupScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "session-cleanup");
            t.setDaemon(true);
            return t;
        });

        startCleanupTask();
    }
    
    /**
     * Constructs a SessionManager with default configuration.
     */
    public SessionManager() {
        this(new SessionConfig());
    }
    
    /**
     * Legacy constructor for backward compatibility.
     * @deprecated Use SessionManager(SessionConfig) instead
     */
    @Deprecated
    public SessionManager(StorageType storageType, Path storagePath, 
            long cleanupIntervalSeconds, int defaultMaxInactiveInterval) {
        this(new SessionConfig()
            .setStorageType(storageType)
            .setStoragePath(storagePath)
            .setCleanupIntervalSeconds(cleanupIntervalSeconds)
            .setDefaultMaxInactiveInterval(defaultMaxInactiveInterval));
    }
    
    public Session createSession() {
        return createSession(config.getDefaultMaxInactiveInterval());
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
            config.getCleanupIntervalSeconds(),
            config.getCleanupIntervalSeconds(),
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
    
    /**
     * Gets the session configuration.
     * 
     * @return The SessionConfig instance
     */
    public SessionConfig getConfig() {
        return config;
    }
    
    /**
     * Creates a session cookie using the configured settings.
     * 
     * @param sessionId The session ID
     * @return Cookie configured according to SessionConfig
     */
    public Cookie createSessionCookie(String sessionId) {
        return config.createSessionCookie(sessionId);
    }
    
    /**
     * Creates a cookie to delete the session cookie.
     * 
     * @return Cookie configured to delete the session
     */
    public Cookie createDeleteCookie() {
        return config.createDeleteCookie();
    }
    
    /**
     * Gets the configured session cookie name.
     * 
     * @return The cookie name (e.g., "JSESSIONID")
     */
    public String getCookieName() {
        return config.getCookieName();
    }
}
