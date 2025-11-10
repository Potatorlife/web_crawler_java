package main.java.com.potato.robots;

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
            URL url = new URL(robotsUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", userAgent);
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);

            int code = conn.getResponseCode();
            if (code != 200) {
                // no robots or not accessible → allow everything
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

                    // User-agent: *
                    if (line.toLowerCase().startsWith("user-agent:")) {
                        String ua = line.substring("user-agent:".length()).trim();
                        inGlobalUserAgent = ua.equals("*");
                    } else if (inGlobalUserAgent && line.toLowerCase().startsWith("disallow:")) {
                        String path = line.substring("disallow:".length()).trim();
                        // empty disallow means allow all
                        if (!path.isEmpty()) {
                            rules.addDisallow(path);
                        }
                    }
                }
            }
        } catch (IOException e) {
            // on error, just allow everything
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