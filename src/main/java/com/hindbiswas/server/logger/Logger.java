package com.hindbiswas.server.logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static volatile LogType logLevel = LogType.INFO;
    private static final long MAX_LOG_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final int MAX_BACKUP_FILES = 5;
    
    private final File logFile;
    private final boolean enableConsole;
    private final boolean enableFile;
    private BufferedWriter fileWriter;
    private final Object writeLock = new Object();
    
    private static volatile Logger INSTANCE = new Logger();

    public Logger() {
        this.logFile = null;
        this.enableConsole = true;
        this.enableFile = false;
        this.fileWriter = null;
    }

    public Logger(String storageDir) {
        this(storageDir, true, true);
    }

    public Logger(String storageDir, boolean enableConsole, boolean enableFile) {
        String fileName = "server.log";
        this.enableConsole = enableConsole;
        this.enableFile = enableFile;
        
        if (enableFile) {
            Path dir = Paths.get(storageDir);
            try {
                Files.createDirectories(dir);
                this.logFile = dir.resolve(fileName).toFile();
                if (!logFile.exists()) {
                    logFile.createNewFile();
                }
                // Initialize BufferedWriter once
                this.fileWriter = new BufferedWriter(new FileWriter(logFile, true));
            } catch (IOException e) {
                throw new RuntimeException("Failed to initialize log file directory: " + storageDir, e);
            }
        } else {
            this.logFile = null;
            this.fileWriter = null;
        }
    }

    public static synchronized void initialize(String storageDir) {
        initialize(storageDir, true, true);
    }

    public static synchronized void initialize(String storageDir, boolean enableConsole, boolean enableFile) {
        if (INSTANCE.logFile != null) {
            // Close existing logger
            INSTANCE.close();
        }
        INSTANCE = new Logger(storageDir, enableConsole, enableFile);
    }

    public static synchronized void setLogLevel(LogType logLevel) {
        Logger.logLevel = logLevel;
    }

    private synchronized void log(LogType logType, String message, Object... args) {
        if (logType.ordinal() < logLevel.ordinal())
            return;

        String formattedMessageConsole = null;
        String formattedMessageFile = null;

        if (enableConsole) {
            formattedMessageConsole = formatMessageForConsole(logType, message, args);
        }
        
        if (enableFile && logFile != null) {
            formattedMessageFile = formatMessageForFile(logType, message, args);
        }

        // Write to console
        if (enableConsole && formattedMessageConsole != null) {
            if (logType == LogType.ERROR)
                System.err.print(formattedMessageConsole);
            else
                System.out.print(formattedMessageConsole);
        }

        // Write to file
        if (enableFile && formattedMessageFile != null) {
            writeToFile(formattedMessageFile);
        }
    }

    private String formatMessageForConsole(LogType logType, String message, Object... args) {
        StringBuilder sb = new StringBuilder();

        LocalDateTime now = LocalDateTime.now();
        sb.append("[");
        sb.append(TextFormatter.cyan(FORMATTER.format(now)));
        sb.append("] ");

        sb.append(switch (logType) {
            case DEBUG -> TextFormatter.cyanLabel(" DEBUG ");
            case INFO -> TextFormatter.greenLabel(" INFO  ");
            case WARN -> TextFormatter.yellowLabel(" WARN  ");
            case ERROR -> TextFormatter.redLabel(" ERROR ");
        });

        sb.append(" ");
        sb.append(message);
        if (args.length > 0) {
            String indent = "\n" + " ".repeat(30); // Dynamic spacing
            for (Object arg : args) {
                sb.append(indent);
                sb.append(arg);
            }
        }

        sb.append("\n");
        return sb.toString();
    }

    private String formatMessageForFile(LogType logType, String message, Object... args) {
        StringBuilder sb = new StringBuilder();

        LocalDateTime now = LocalDateTime.now();
        sb.append("[");
        sb.append(FORMATTER.format(now));
        sb.append("] ");

        sb.append(switch (logType) {
            case DEBUG -> "[ DEBUG ]";
            case INFO -> "[ INFO  ]";
            case WARN -> "[ WARN  ]";
            case ERROR -> "[ ERROR ]";
        });

        sb.append(" ");
        sb.append(message);
        if (args.length > 0) {
            String indent = "\n" + " ".repeat(30); // Dynamic spacing
            for (Object arg : args) {
                sb.append(indent);
                sb.append(arg);
            }
        }

        sb.append("\n");
        return sb.toString();
    }

    private void writeToFile(String logMessage) {
        synchronized (writeLock) {
            try {
                // Check if log rotation is needed
                if (logFile.length() > MAX_LOG_FILE_SIZE) {
                    rotateLogFile();
                }
                
                if (fileWriter != null) {
                    fileWriter.write(logMessage);
                    fileWriter.flush();
                }
            } catch (IOException e) {
                System.err.println("Failed to write to log file: " + e.getMessage());
            }
        }
    }

    private void rotateLogFile() {
        try {
            if (fileWriter != null) {
                fileWriter.close();
            }
            for (int i = MAX_BACKUP_FILES - 1; i > 0; i--) {
                File oldFile = new File(logFile.getAbsolutePath() + "." + i);
                File newFile = new File(logFile.getAbsolutePath() + "." + (i + 1));
                if (oldFile.exists()) {
                    if (newFile.exists()) {
                        newFile.delete();
                    }
                    oldFile.renameTo(newFile);
                }
            }

            File backup = new File(logFile.getAbsolutePath() + ".1");
            if (backup.exists()) {
                backup.delete();
            }
            logFile.renameTo(backup);

            logFile.createNewFile();
            fileWriter = new BufferedWriter(new FileWriter(logFile, true));
            
            String rotationMsg = formatMessageForFile(LogType.INFO, "Log file rotated");
            fileWriter.write(rotationMsg);
            fileWriter.flush();
        } catch (IOException e) {
            System.err.println("Failed to rotate log file: " + e.getMessage());
        }
    }

    public synchronized void close() {
        if (fileWriter != null) {
            try {
                fileWriter.close();
            } catch (IOException e) {
                System.err.println("Failed to close log file: " + e.getMessage());
            }
        }
    }

    public void info(String message, Object... args) {
        log(LogType.INFO, message, args);
    }

    public void debug(String message, Object... args) {
        log(LogType.DEBUG, message, args);
    }

    public void warn(String message, Object... args) {
        log(LogType.WARN, message, args);
    }

    public void error(String message, Object... args) {
        log(LogType.ERROR, message, args);
    }

    public static void log(String message, Object... args) {
        INSTANCE.info(message, args);
    }

    public static void dbg(String message, Object... args) {
        INSTANCE.debug(message, args);
    }

    public static void wrn(String message, Object... args) {
        INSTANCE.warn(message, args);
    }

    public static void err(String message, Object... args) {
        INSTANCE.error(message, args);
    }
}
