package com.hindbiswas.server.session.storage;

import com.hindbiswas.server.session.Session;
import java.util.Optional;


public interface SessionStorage {
    /**
     * Saves a session to the storage.
     * @param session The session to save
     */
    void save(Session session);
    
    /**
     * Loads a session by its ID.
     * @param id The session ID
     * @return An Optional containing the session if found
     */
    Optional<Session> load(String id);
    
    /**
     * Deletes a session from storage.
     * @param id The session ID to delete
     */
    void delete(String id);
    
    /**
     * Performs cleanup of expired sessions.
     * @return Number of sessions cleaned up
     */
    int cleanup();
    
    /**
     * Initializes the storage.
     */
    void initialize();
    
    /**
     * Shuts down the storage, performing any necessary cleanup.
     */
    void shutdown();
}
