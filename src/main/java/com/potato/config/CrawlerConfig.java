package com.potato.config;

import java.util.Collections;
import java.util.Set;

public class CrawlerConfig {

    private final int maxPages;
    private final int maxDepth;
    private final String userAgent;
    private final long politenessMs;
    private final Set<String> allowedHosts;

    public CrawlerConfig(int maxPages,
                         int maxDepth,
                         String userAgent,
                         long politenessMs,
                         Set<String> allowedHosts) {
        this.maxPages = maxPages;
        this.maxDepth = maxDepth;
        this.userAgent = userAgent;
        this.politenessMs = politenessMs;
        this.allowedHosts = (allowedHosts == null)
                ? Collections.emptySet()
                : Collections.unmodifiableSet(allowedHosts);
    }

    public int getMaxPages() {
        return maxPages;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public long getPolitenessMs() {
        return politenessMs;
    }

    public Set<String> getAllowedHosts() {
        return allowedHosts;
    }

    public boolean hasHostWhitelist() {
        return !allowedHosts.isEmpty();
    }
}