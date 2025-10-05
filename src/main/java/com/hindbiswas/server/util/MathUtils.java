package com.hindbiswas.server.util;

/**
 * Math utility functions for JHP templates.
 * Each method is a SAM (Single Abstract Method) functional interface.
 */
public class MathUtils {

    /**
     * Returns the absolute value of a number.
     */
    @FunctionalInterface
    public interface Abs {
        double apply(Number n);
    }

    /**
     * Rounds a number to the nearest integer.
     */
    @FunctionalInterface
    public interface Round {
        long apply(Number n);
    }

    /**
     * Returns the ceiling of a number.
     */
    @FunctionalInterface
    public interface Ceil {
        double apply(Number n);
    }

    /**
     * Returns the floor of a number.
     */
    @FunctionalInterface
    public interface Floor {
        double apply(Number n);
    }

    /**
     * Returns the maximum of two numbers.
     */
    @FunctionalInterface
    public interface Max {
        double apply(Number a, Number b);
    }

    /**
     * Returns the minimum of two numbers.
     */
    @FunctionalInterface
    public interface Min {
        double apply(Number a, Number b);
    }

    /**
     * Returns a number raised to a power.
     */
    @FunctionalInterface
    public interface Pow {
        double apply(Number base, Number exponent);
    }

    /**
     * Returns the square root of a number.
     */
    @FunctionalInterface
    public interface Sqrt {
        double apply(Number n);
    }

    /**
     * Returns a random number between 0 (inclusive) and max (exclusive).
     */
    @FunctionalInterface
    public interface Random {
        int apply(int max);
    }

    /**
     * Clamps a number between min and max.
     */
    @FunctionalInterface
    public interface Clamp {
        double apply(Number value, Number min, Number max);
    }

    // Implementations
    public static final Abs abs = n -> n == null ? 0 : Math.abs(n.doubleValue());
    public static final Round round = n -> n == null ? 0 : Math.round(n.doubleValue());
    public static final Ceil ceil = n -> n == null ? 0 : Math.ceil(n.doubleValue());
    public static final Floor floor = n -> n == null ? 0 : Math.floor(n.doubleValue());
    public static final Max max = (a, b) -> {
        double da = a == null ? 0 : a.doubleValue();
        double db = b == null ? 0 : b.doubleValue();
        return Math.max(da, db);
    };
    public static final Min min = (a, b) -> {
        double da = a == null ? 0 : a.doubleValue();
        double db = b == null ? 0 : b.doubleValue();
        return Math.min(da, db);
    };
    public static final Pow pow = (base, exponent) -> {
        double b = base == null ? 0 : base.doubleValue();
        double e = exponent == null ? 0 : exponent.doubleValue();
        return Math.pow(b, e);
    };
    public static final Sqrt sqrt = n -> n == null ? 0 : Math.sqrt(n.doubleValue());
    public static final Random random = max -> max <= 0 ? 0 : (int) (Math.random() * max);
    public static final Clamp clamp = (value, min, max) -> {
        double v = value == null ? 0 : value.doubleValue();
        double minVal = min == null ? 0 : min.doubleValue();
        double maxVal = max == null ? 0 : max.doubleValue();
        return Math.max(minVal, Math.min(v, maxVal));
    };
}
