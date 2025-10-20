package com.hindbiswas.server.session;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.hindbiswas.server.http.Cookie;

/**
 * Configuration for session management in the web server.
 * Provides builder-style API for configuring session behavior.
 */
public class SessionConfig {
    private StorageType storageType = StorageType.MEMORY;
    private Path storagePath = Paths.get(".sessions");
    private int defaultMaxInactiveInterval = 1800; // 30 minutes
    private long cleanupIntervalSeconds = 120; // 2 minutes

    // Cookie configuration
    private String cookieName = "JSESSIONID";
    private boolean cookieHttpOnly = true;
    private boolean cookieSecure = false;
    private Cookie.SameSite cookieSameSite = Cookie.SameSite.LAX;
    private String cookiePath = "/";
    private String cookieDomain = null;

    public SessionConfig() {
    }

    // Storage configuration

    public SessionConfig setStorageType(StorageType storageType) {
        this.storageType = storageType;
        return this;
    }

    public SessionConfig setStoragePath(String storagePath) {
        this.storagePath = Paths.get(storagePath);
        return this;
    }

    public SessionConfig setStoragePath(Path storagePath) {
        this.storagePath = storagePath;
        return this;
    }

    public SessionConfig setDefaultMaxInactiveInterval(int seconds) {
        this.defaultMaxInactiveInterval = seconds;
        return this;
    }

    public SessionConfig setCleanupIntervalSeconds(long seconds) {
        this.cleanupIntervalSeconds = seconds;
        return this;
    }

    // Cookie configuration

    public SessionConfig setCookieName(String cookieName) {
        this.cookieName = cookieName;
        return this;
    }

    public SessionConfig setCookieHttpOnly(boolean httpOnly) {
        this.cookieHttpOnly = httpOnly;
        return this;
    }

    public SessionConfig setCookieSecure(boolean secure) {
        this.cookieSecure = secure;
        return this;
    }

    public SessionConfig setCookieSameSite(Cookie.SameSite sameSite) {
        this.cookieSameSite = sameSite;
        return this;
    }

    public SessionConfig setCookiePath(String path) {
        this.cookiePath = path;
        return this;
    }

    public SessionConfig setCookieDomain(String domain) {
        this.cookieDomain = domain;
        return this;
    }

    // Getters

    public StorageType getStorageType() {
        return storageType;
    }

    public Path getStoragePath() {
        return storagePath;
    }

    public int getDefaultMaxInactiveInterval() {
        return defaultMaxInactiveInterval;
    }

    public long getCleanupIntervalSeconds() {
        return cleanupIntervalSeconds;
    }

    public String getCookieName() {
        return cookieName;
    }

    public boolean isCookieHttpOnly() {
        return cookieHttpOnly;
    }

    public boolean isCookieSecure() {
        return cookieSecure;
    }

    public Cookie.SameSite getCookieSameSite() {
        return cookieSameSite;
    }

    public String getCookiePath() {
        return cookiePath;
    }

    public String getCookieDomain() {
        return cookieDomain;
    }

    /**
     * Creates a session cookie with the configured settings.
     */
    public Cookie createSessionCookie(String sessionId) {
        Cookie cookie = new Cookie(cookieName, sessionId);
        cookie.setHttpOnly(cookieHttpOnly);
        cookie.setSecure(cookieSecure);
        cookie.setSameSite(cookieSameSite);
        cookie.setPath(cookiePath);
        if (cookieDomain != null) {
            cookie.setDomain(cookieDomain);
        }
        cookie.setMaxAge(-1); // Session cookie
        return cookie;
    }

    /**
     * Creates a cookie to delete the session cookie.
     */
    public Cookie createDeleteCookie() {
        Cookie cookie = new Cookie(cookieName, "");
        cookie.setPath(cookiePath);
        if (cookieDomain != null) {
            cookie.setDomain(cookieDomain);
        }
        cookie.setMaxAge(0); // Delete immediately
        return cookie;
    }
}
