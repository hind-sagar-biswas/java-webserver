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
    private static LogType logLevel = LogType.INFO;
    private final File logFile;
    private static final Logger INSTANCE = new Logger();

    public Logger() {
        this.logFile = null;
    }

    public Logger(String storageDir) {
        String fileName = "server.log";
        Path dir = Paths.get(storageDir);
        try {
            Files.createDirectories(dir);
            this.logFile = dir.resolve(fileName).toFile();
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize session storage directory", e);
        }

    }

    public static void setLogLevel(LogType logLevel) {
        Logger.logLevel = logLevel;
    }

    private void log(LogType logType, String message, Object... args) {
        if (logType.ordinal() < logLevel.ordinal())
            return;

        if (logFile != null) {
            String formattedMessage = formatMessageForFile(logType, message, args);
            writeToFile(formattedMessage);
        } else {
            String formattedMessage = formatMessageForConsole(logType, message, args);

            if (logType == LogType.ERROR)
                System.err.print(formattedMessage);
            else
                System.out.print(formattedMessage);
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
            for (Object arg : args) {
                sb.append("\n                              ");
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
            for (Object arg : args) {
                sb.append("\n                              ");
                sb.append(arg);
            }
        }

        sb.append("\n");
        return sb.toString();
    }

    private void writeToFile(String logMessage) {
        try (FileWriter writer = new FileWriter(logFile, true)) {
            BufferedWriter bw = new BufferedWriter(writer);
            bw.write(logMessage);
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
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
