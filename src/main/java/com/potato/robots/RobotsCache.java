package com.potato.robots;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RobotsCache {

    private final RobotsFetcher fetcher;
    private final Map<String, RobotsRules> cache = new ConcurrentHashMap<>();

    public RobotsCache(RobotsFetcher fetcher) {
        this.fetcher = fetcher;
    }

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