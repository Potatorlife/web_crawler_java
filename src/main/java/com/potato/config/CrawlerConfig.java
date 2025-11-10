package com.potato.config;

import java.util.Collections;
import java.util.Set;

public class CrawlerConfig {

    private final int maxPages;
    private final int maxDepth;
    private final String userAgent;
    private final long politenessMs;
    private final Set<String> allowedHosts;

    private final int fetchRetries;
    private final long fetchBackoffMs;
    private final String proxyHost;
    private final int proxyPort;

    public CrawlerConfig(int maxPages,
                         int maxDepth,
                         String userAgent,
                         long politenessMs,
                         Set<String> allowedHosts,
                         int fetchRetries,
                         long fetchBackoffMs,
                         String proxyHost,
                         int proxyPort) {
        this.maxPages = maxPages;
        this.maxDepth = maxDepth;
        this.userAgent = userAgent;
        this.politenessMs = politenessMs;

        // make sure allowedHosts is never null and cannot be modified from outside
        this.allowedHosts = (allowedHosts == null)
                ? Collections.emptySet()
                : Collections.unmodifiableSet(allowedHosts);

        this.fetchRetries = fetchRetries;
        this.fetchBackoffMs = fetchBackoffMs;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
    }

    // simple getters for all the config values
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

    public int getFetchRetries() {
        return fetchRetries;
    }

    public long getFetchBackoffMs() {
        return fetchBackoffMs;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    // convenience: check if proxy settings are usable
    public boolean hasProxy() {
        return proxyHost != null && !proxyHost.isBlank() && proxyPort > 0;
    }
}
