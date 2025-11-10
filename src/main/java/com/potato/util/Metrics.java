package com.potato.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class Metrics {
    public final AtomicLong pagesFetched = new AtomicLong();
    public final AtomicLong pagesFailed  = new AtomicLong();
    public final AtomicLong pagesSkipped = new AtomicLong();
    public final AtomicLong bytesFetched = new AtomicLong();
    public final AtomicLong inFlight     = new AtomicLong();

    private final AtomicLong fetchCount       = new AtomicLong();
    private final AtomicLong totalFetchMillis = new AtomicLong();

    public final ConcurrentHashMap<String, AtomicLong> errorsByHost = new ConcurrentHashMap<>();

    private final ScheduledExecutorService reporter = Executors.newSingleThreadScheduledExecutor();

    public void startReporting() {
        reporter.scheduleAtFixedRate(this::printSnapshot, 30, 30, TimeUnit.SECONDS);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n=== Crawl summary ===");
            printSnapshot();
            reporter.shutdownNow();
        }));
    }

    public void recordFetch(long millis, long bytes) {
        pagesFetched.incrementAndGet();
        fetchCount.incrementAndGet();
        totalFetchMillis.addAndGet(millis);
        bytesFetched.addAndGet(bytes);
    }

    public void recordFailure(String host) {
        pagesFailed.incrementAndGet();
        errorsByHost
                .computeIfAbsent(host, h -> new AtomicLong())
                .incrementAndGet();
    }

    public void recordSkip() {
        pagesSkipped.incrementAndGet();
    }

    private void printSnapshot() {
        long fetched = pagesFetched.get();
        long failed  = pagesFailed.get();
        long skipped = pagesSkipped.get();
        long infl    = inFlight.get();
        long count   = fetchCount.get();
        long avgMs   = (count == 0) ? 0 : (totalFetchMillis.get() / count);
        long mb      = bytesFetched.get() / (1024 * 1024);

        System.out.printf(
                "METRICS | fetched=%d failed=%d skipped=%d inflight=%d avgFetchMs=%d data=%dMB%n",
                fetched, failed, skipped, infl, avgMs, mb
        );
    }
}
