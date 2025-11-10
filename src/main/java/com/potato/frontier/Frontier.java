package com.potato.frontier;

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
