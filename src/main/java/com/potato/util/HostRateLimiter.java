package com.potato.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple per-host rate limiter.
 *
 * Ensures that we wait at least `delayMs` between two requests
 * to the same host. This is a politeness mechanism so we don't
 * spam a single website when crawling.
 */
public class HostRateLimiter {

    private final Map<String, Long> lastFetch = new HashMap<>();
    private final long delayMs;

    public HostRateLimiter(long delayMs) {
        this.delayMs = delayMs;
    }

    /**
     * Block the current thread until it's okay to fetch from this host again.
     * Synchronized so multiple threads don't fetch the same host too quickly.
     */
    public synchronized void acquire(String host) {
        long now = System.currentTimeMillis();
        long last = lastFetch.getOrDefault(host, 0L);
        long wait = last + delayMs - now;
        if (wait > 0) {
            try {
                Thread.sleep(wait);
            } catch (InterruptedException ignored) {
            }
        }
        lastFetch.put(host, System.currentTimeMillis());
    }
}
