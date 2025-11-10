package main.java.com.potato.config;

public class CrawlerConfig {
    private final int maxPages;
    private final int maxDepth;
    private final String userAgent;

    public CrawlerConfig(int maxPages, int maxDepth, String userAgent) {
        this.maxPages = maxPages;
        this.maxDepth = maxDepth;
        this.userAgent = userAgent;
    }

    public int getMaxPages() { return maxPages; }
    public int getMaxDepth() { return maxDepth; }
    public String getUserAgent() { return userAgent; }
}
