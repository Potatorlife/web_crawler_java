package com.potato.robots;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RobotsCache {

    // component that actually fetches and parses robots.txt from the web
    private final RobotsFetcher fetcher;
    
    // in-memory cache: host -> parsed robots rules
    private final Map<String, RobotsRules> cache = new ConcurrentHashMap<>();

    public RobotsCache(RobotsFetcher fetcher) {
        this.fetcher = fetcher;
    }

    /**
     * Returns the robots.txt rules for the given URL.
     * If we already fetched rules for that host, return the cached version.
     * Otherwise, fetch and parse robots.txt once and store it.
     */
    public RobotsRules getRulesFor(String url) {
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme() == null ? "https" : uri.getScheme();
            String host = uri.getHost();
            if (host == null) {
                return new RobotsRules();
            }
            String key = scheme + "://" + host;

            return cache.computeIfAbsent(key, k -> fetcher.fetchForHost(scheme, host));
        } catch (Exception e) {
            return new RobotsRules();
        }
    }
}