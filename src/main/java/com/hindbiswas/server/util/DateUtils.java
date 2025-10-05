package com.hindbiswas.server.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Date and time utility functions for JHP templates.
 * Each method is a SAM (Single Abstract Method) functional interface.
 */
public class DateUtils {

    /**
     * Returns the current timestamp in milliseconds.
     */
    @FunctionalInterface
    public interface Now {
        long apply();
    }

    /**
     * Formats a timestamp (in milliseconds) to a string.
     */
    @FunctionalInterface
    public interface FormatDate {
        String apply(long timestamp, String pattern);
    }

    /**
     * Returns the current date and time as a formatted string.
     */
    @FunctionalInterface
    public interface CurrentDateTime {
        String apply(String pattern);
    }

    /**
     * Returns the current year.
     */
    @FunctionalInterface
    public interface CurrentYear {
        int apply();
    }

    /**
     * Returns the current month (1-12).
     */
    @FunctionalInterface
    public interface CurrentMonth {
        int apply();
    }

    /**
     * Returns the current day of month.
     */
    @FunctionalInterface
    public interface CurrentDay {
        int apply();
    }

    // Implementations
    public static final Now now = () -> System.currentTimeMillis();
    
    public static final FormatDate formatDate = (timestamp, pattern) -> {
        try {
            LocalDateTime dateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(timestamp), 
                ZoneId.systemDefault()
            );
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                pattern == null || pattern.isEmpty() ? "yyyy-MM-dd HH:mm:ss" : pattern
            );
            return dateTime.format(formatter);
        } catch (Exception e) {
            return "Invalid date or pattern";
        }
    };
    
    public static final CurrentDateTime currentDateTime = pattern -> {
        try {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                pattern == null || pattern.isEmpty() ? "yyyy-MM-dd HH:mm:ss" : pattern
            );
            return now.format(formatter);
        } catch (Exception e) {
            return "Invalid pattern";
        }
    };
    
    public static final CurrentYear currentYear = () -> LocalDateTime.now().getYear();
    public static final CurrentMonth currentMonth = () -> LocalDateTime.now().getMonthValue();
    public static final CurrentDay currentDay = () -> LocalDateTime.now().getDayOfMonth();
}
