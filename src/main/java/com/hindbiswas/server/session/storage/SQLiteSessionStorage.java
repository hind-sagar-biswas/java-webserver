package com.hindbiswas.server.session.storage;

import com.hindbiswas.server.logger.Logger;
import com.hindbiswas.server.session.Session;
import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.Optional;


public class SQLiteSessionStorage implements SessionStorage {
    private static final String CREATE_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS sessions (
                id TEXT PRIMARY KEY,
                data BLOB NOT NULL,
                last_accessed INTEGER NOT NULL,
                expires_at INTEGER NOT NULL
            )
            """;

    private static final String CREATE_INDEX_SQL = "CREATE INDEX IF NOT EXISTS idx_expires_at ON sessions(expires_at)";

    private static final String UPSERT_SQL = """
            INSERT INTO sessions (id, data, last_accessed, expires_at)
            VALUES (?, ?, ?, ?)
            ON CONFLICT(id) DO UPDATE SET
                data = excluded.data,
                last_accessed = excluded.last_accessed,
                expires_at = excluded.expires_at
            """;

    private static final String SELECT_SQL = "SELECT data FROM sessions WHERE id = ? AND expires_at > ?";

    private static final String DELETE_SQL = "DELETE FROM sessions WHERE id = ?";
    private static final String CLEANUP_SQL = "DELETE FROM sessions WHERE expires_at <= ?";

    private final String connectionUrl;
    private Connection connection;

    public SQLiteSessionStorage(Path dbPath) {
        String dbFileName = dbPath != null ? dbPath.resolve("sessions.db").toString() : "sessions.db";
        this.connectionUrl = "jdbc:sqlite:" + dbFileName;
    }

    @Override
    public synchronized void initialize() {
        try {
            Class.forName("org.sqlite.JDBC");

            if (connectionUrl.startsWith("jdbc:sqlite:")) {
                String path = connectionUrl.substring("jdbc:sqlite:".length());
                Path parent = Path.of(path).getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
            }

            connection = DriverManager.getConnection(connectionUrl);
            connection.setAutoCommit(false);

            try (Statement stmt = connection.createStatement()) {
                stmt.execute(CREATE_TABLE_SQL);
                stmt.execute(CREATE_INDEX_SQL);
                connection.commit();
            }
        } catch (Exception e) {
            Logger.err("Failed to initialize SQLite storage: " + e.getMessage());
            throw new RuntimeException("Failed to initialize SQLite storage", e);
        }
    }

    @Override
    public synchronized void save(Session session) {
        try (PreparedStatement stmt = connection.prepareStatement(UPSERT_SQL)) {
            stmt.setString(1, session.getId());
            stmt.setBytes(2, serialize(session));
            stmt.setLong(3, session.getLastAccessedTime());
            stmt.setLong(4, calculateExpirationTime(session));
            stmt.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            rollbackQuietly();
            Logger.err("Failed to save session " + session.getId() + ": " + e.getMessage());
            throw new RuntimeException("Failed to save session: " + session.getId(), e);
        } catch (IOException e) {
            rollbackQuietly();
            Logger.err("Failed to serialize session " + session.getId() + ": " + e.getMessage());
            throw new RuntimeException("Failed to serialize session: " + session.getId(), e);
        }
    }

    @Override
    public synchronized Optional<Session> load(String id) {
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_SQL)) {
            stmt.setString(1, id);
            stmt.setLong(2, System.currentTimeMillis());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    byte[] data = rs.getBytes("data");
                    return Optional.of(deserialize(data));
                }
            }
            return Optional.empty();
        } catch (SQLException | IOException | ClassNotFoundException e) {
            Logger.err("Failed to load session " + id + ": " + e.getMessage());
            throw new RuntimeException("Failed to load session: " + id, e);
        }
    }

    @Override
    public synchronized void delete(String id) {
        try (PreparedStatement stmt = connection.prepareStatement(DELETE_SQL)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            rollbackQuietly();
            Logger.err("Failed to delete session " + id + ": " + e.getMessage());
            throw new RuntimeException("Failed to delete session: " + id, e);
        }
    }

    @Override
    public synchronized int cleanup() {
        try (PreparedStatement stmt = connection.prepareStatement(CLEANUP_SQL)) {
            stmt.setLong(1, System.currentTimeMillis());
            int count = stmt.executeUpdate();
            connection.commit();
            return count;
        } catch (SQLException e) {
            rollbackQuietly();
            Logger.err("Failed to clean up expired sessions: " + e.getMessage());
            throw new RuntimeException("Failed to clean up expired sessions", e);
        }
    }

    @Override
    public synchronized void shutdown() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                Logger.err("Failed to close SQLite connection: " + e.getMessage());
            }
        }
    }

    private long calculateExpirationTime(Session session) {
        if (session.getMaxInactiveInterval() < 0) {
            return Long.MAX_VALUE; // Never expires
        }
        return session.getLastAccessedTime() + (session.getMaxInactiveInterval() * 1000L);
    }

    private byte[] serialize(Session session) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                ObjectOutputStream stream = new ObjectOutputStream(out)) {
            stream.writeObject(session);
            return out.toByteArray();
        }
    }

    private Session deserialize(byte[] data) throws IOException, ClassNotFoundException {
        try (ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(data))) {
            return (Session) stream.readObject();
        }
    }

    private void rollbackQuietly() {
        try {
            if (connection != null) {
                connection.rollback();
            }
        } catch (SQLException ex) {
            Logger.err("Failed to rollback transaction: " + ex.getMessage());
        }
    }
}
