package com.potato.robots;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.HttpURLConnection;

public class RobotsFetcher {

    private final String userAgent;

    public RobotsFetcher(String userAgent) {
        this.userAgent = userAgent;
    }

    public RobotsRules fetchForHost(String scheme, String host) {
        RobotsRules rules = new RobotsRules();

        String robotsUrl = scheme + "://" + host + "/robots.txt";

        try {
            URI robotsUri = URI.create(robotsUrl);
            URL url = robotsUri.toURL();

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", userAgent);
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);

            int code = conn.getResponseCode();
            if (code != 200) {
                return rules;
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                boolean inGlobalUserAgent = false;
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();

                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }

                    String lower = line.toLowerCase();

                    if (lower.startsWith("user-agent:")) {
                        String ua = line.substring("user-agent:".length()).trim();
                        inGlobalUserAgent = ua.equals("*");

                    } else if (inGlobalUserAgent && lower.startsWith("disallow:")) {
                        String path = line.substring("disallow:".length()).trim();
                        if (!path.isEmpty()) {
                            rules.addDisallow(path);
                        }

                    } else if (lower.startsWith("sitemap:")) {
                        String sm = line.substring("sitemap:".length()).trim();
                        if (!sm.isEmpty()) {
                            rules.addSitemap(sm);
                        }
                    }
                }
            }
        } catch (IOException e) {
            return rules;
        }

        return rules;
    }

    public RobotsRules fetchForUrl(String urlStr) {
        try {
            URI uri = URI.create(urlStr);
            String scheme = uri.getScheme() == null ? "https" : uri.getScheme();
            String host = uri.getHost();
            if (host == null) {
                return new RobotsRules();
            }
            return fetchForHost(scheme, host);
        } catch (Exception e) {
            return new RobotsRules();
        }
    }
}
