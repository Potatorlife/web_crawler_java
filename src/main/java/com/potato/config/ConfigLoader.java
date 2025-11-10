package com.potato.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class ConfigLoader {

    public static CrawlerConfig load() {
        // try-with-resources: load the main config file from the classpath
        try (InputStream in = ConfigLoader.class.getClassLoader()
                .getResourceAsStream("application.properties")) {

            Properties props = new Properties();
            // if the file exists, load all key/value pairs
            if (in != null) {
                props.load(in);
            }

            // read basic crawler settings, with sensible defaults if missing
            int maxPages = Integer.parseInt(props.getProperty("crawler.maxPages", "100"));
            int maxDepth = Integer.parseInt(props.getProperty("crawler.maxDepth", "2"));
            String userAgent = props.getProperty("crawler.userAgent", "PotatorCrawler/1.0");
            long politenessMs = Long.parseLong(props.getProperty("crawler.politenessMs", "500"));
            String allowedHostsFile = props.getProperty("crawler.allowedHostsFile", null);

            int fetchRetries = Integer.parseInt(props.getProperty("crawler.fetchRetries", "3"));
            long fetchBackoffMs = Long.parseLong(props.getProperty("crawler.fetchBackoffMs", "500"));
            String proxyHost = props.getProperty("crawler.proxyHost", "");
            int proxyPort = Integer.parseInt(props.getProperty("crawler.proxyPort", "0"));

            // if a separate allowed-hosts file is defined, read it line by line
            Set<String> allowedHosts = new HashSet<>();
            if (allowedHostsFile != null) {
                try (InputStream hostsIn = ConfigLoader.class.getClassLoader()
                        .getResourceAsStream(allowedHostsFile)) {
                    if (hostsIn != null) {
                        try (BufferedReader br = new BufferedReader(new InputStreamReader(hostsIn))) {
                            String line;
                            while ((line = br.readLine()) != null) {
                                line = line.trim();
                                if (!line.isEmpty() && !line.startsWith("#")) {
                                    allowedHosts.add(line.toLowerCase());
                                }
                            }
                        }
                    }
                }
            }

            // finally, build the immutable CrawlerConfig object with everything we read
            return new CrawlerConfig(
                    maxPages,
                    maxDepth,
                    userAgent,
                    politenessMs,
                    allowedHosts,
                    fetchRetries,
                    fetchBackoffMs,
                    proxyHost,
                    proxyPort
            );

        } catch (IOException e) {
            // wrap checked exception so callers don't have to handle it
            throw new RuntimeException("Failed to load configuration", e);
        }
    }
}
