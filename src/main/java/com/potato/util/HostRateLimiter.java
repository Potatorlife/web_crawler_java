package main.java.com.potato.util;

import java.util.HashMap;
import java.util.Map;

public class HostRateLimiter {

    private final Map<String, Long> lastFetch = new HashMap<>();
    private final long delayMs;

    public HostRateLimiter(long delayMs) {
        this.delayMs = delayMs;
    }

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
