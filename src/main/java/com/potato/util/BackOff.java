package com.potato.util;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Utility for computing a backoff delay between retry attempts.
 *
 * Uses an exponential backoff (capped) + full jitter:
 * - try 1: somewhere between 0 and baseMs
 * - try 2: somewhere between 0 and 2*baseMs
 * - ...
 * - but never more than maxMs
 *
 * This helps to avoid hammering the same host repeatedly and to spread
 * requests across time when many threads are retrying.
 */
public class BackOff {
    public static long computeDelayMs(int attempt, long baseMs, long maxMs) {
        long exp = Math.min(maxMs, baseMs * (1L << Math.min(20, attempt)));
        return ThreadLocalRandom.current().nextLong(0, Math.max(baseMs, exp) + 1);
    }
}
