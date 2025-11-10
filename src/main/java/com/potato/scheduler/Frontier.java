package com.potato.scheduler;

/**
 * Frontier abstraction: represents "the place where new URLs to crawl are stored".
 * Different implementations can be in-memory, file-backed, DB-backed, etc.
 *
 * Extends AutoCloseable so implementations can flush/close resources.
 */
public interface Frontier extends AutoCloseable {
    void add(String url, int depth);
    CrawlTask poll(long timeoutMs) throws InterruptedException;
    boolean isEmpty();
    @Override
    void close();

    class CrawlTask {
        public final String url;
        public final int depth;

        public CrawlTask(String url, int depth) {
            this.url = url;
            this.depth = depth;
        }
    }
}
