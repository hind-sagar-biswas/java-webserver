package com.hindbiswas.server.util;

import java.util.UUID;

import de.huxhorn.sulky.ulid.ULID;

/**
 * Utility functions for random number generation.
 */
public class RandomUtils {
    private static final ULID ulidGen = new ULID();

    /**
     * Generates a random UUID.
     */
    @FunctionalInterface
    public interface Uuid {
        String apply();
    }

    /**
     * Generates a random ULID.
     */
    @FunctionalInterface
    public interface Ulid {
        String apply();
    }

    /**
     * Generates a random integer.
     */
    @FunctionalInterface
    public interface Random {
        int apply();
    }

    /**
     * Generates a random integer within a specified range.
     */
    @FunctionalInterface
    public interface RandomMax {
        int apply(int max);
    }

    /**
     * Generates a random integer within a specified range.
     */
    @FunctionalInterface
    public interface RandomRange {
        int apply(int min, int max);
    }

    public static final Ulid ulid = () -> ulidGen.nextULID();
    public static final Uuid uuid = () -> UUID.randomUUID().toString();
    public static final Random random = () -> (int) (Math.random() * Integer.MAX_VALUE);
    public static final RandomMax randomMax = max -> max <= 0 ? 0 : (int) (Math.random() * max);
    public static final RandomRange randomRange = (min, max) -> max <= 0 ? 0
            : (int) (Math.random() * (max - min)) + min;
}
