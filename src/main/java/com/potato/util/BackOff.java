package com.potato.util;
import java.util.concurrent.ThreadLocalRandom;

public class BackOff {
    public static long computeDelayMs(int attempt, long baseMs, long maxMs) {
        long exp = Math.min(maxMs, baseMs * (1L << Math.min(20, attempt)));
        return ThreadLocalRandom.current().nextLong(0, Math.max(baseMs, exp) + 1);
    }
}
