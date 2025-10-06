package com.hindbiswas.server.session.storage;

import com.hindbiswas.server.session.Session;
import java.io.*;
import java.nio.file.*;
import java.util.Optional;

public class FileSessionStorage implements SessionStorage {
    private final Path storageDir;
    private static final String FILE_EXTENSION = ".jssid";

    public FileSessionStorage(Path storagePath) {
        this.storageDir = storagePath != null ? storagePath : Paths.get("sessions");
    }

    @Override
    public void initialize() {
        try {
            Files.createDirectories(storageDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize session storage directory", e);
        }
    }

    @Override
    public void save(Session session) {
        Path sessionFile = getSessionFile(session.getId());
        try (ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(sessionFile.toFile()))) {
            stream.writeObject(session);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save session: " + session.getId(), e);
        }
    }

    @Override
    public Optional<Session> load(String id) {
        Path sessionFile = getSessionFile(id);
        if (!Files.exists(sessionFile)) {
            return Optional.empty();
        }

        try (ObjectInputStream istream = new ObjectInputStream(new FileInputStream(sessionFile.toFile()))) {
            return Optional.of((Session) istream.readObject());
        } catch (IOException | ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    @Override
    public void delete(String id) {
        try {
            Files.deleteIfExists(getSessionFile(id));
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete session: " + id, e);
        }
    }

    @Override
    public int cleanup() {
        int count = 0;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(storageDir, "*" + FILE_EXTENSION)) {
            for (Path path : stream) {
                try (ObjectInputStream istream = new ObjectInputStream(
                        new FileInputStream(path.toFile()))) {
                    Session session = (Session) istream.readObject();
                    if (session.isExpired()) {
                        Files.delete(path);
                        count++;
                    }
                } catch (IOException | ClassNotFoundException e) {
                    try {
                        Files.deleteIfExists(path);
                        count++;
                    } catch (IOException ex) {
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to clean up sessions", e);
        }
        return count;
    }

    @Override
    public void shutdown() {
        // No resources to clean up
    }

    private Path getSessionFile(String sessionId) {
        return storageDir.resolve(sessionId + FILE_EXTENSION);
    }
}
