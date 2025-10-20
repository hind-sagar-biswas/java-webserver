package com.hindbiswas.server.util;

import java.util.UUID;

import de.huxhorn.sulky.ulid.ULID;

public class RandomUtils {
    private static final ULID ulidGen = new ULID();

    @FunctionalInterface
    public interface Uuid {
        String apply();
    }

    @FunctionalInterface
    public interface Ulid {
        String apply();
    }

    @FunctionalInterface
    public interface Random {
        int apply();
    }
    
    @FunctionalInterface
    public interface RandomMax {
        int apply(int max);
    }

    @FunctionalInterface
    public interface RandomRange {
        int apply(int min, int max);
    }

    public static final Ulid ulid = () -> ulidGen.nextULID();
    public static final Uuid uuid = () -> UUID.randomUUID().toString();
    public static final Random random = () -> (int) (Math.random() * Integer.MAX_VALUE);
    public static final RandomMax randomMax = max -> max <= 0 ? 0 : (int) (Math.random() * max);
    public static final RandomRange randomRange = (min, max) -> max <= 0 ? 0 : (int) (Math.random() * (max - min)) + min;
}
