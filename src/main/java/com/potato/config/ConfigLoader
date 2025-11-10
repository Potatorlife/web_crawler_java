package main.java.com.potato.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class ConfigLoader {

    public static CrawlerConfig load() {
        try (InputStream in = ConfigLoader.class.getClassLoader()
                .getResourceAsStream("application.properties")) {

            Properties props = new Properties();
            if (in != null) {
                props.load(in);
            }

            int maxPages = Integer.parseInt(props.getProperty("crawler.maxPages", "100"));
            int maxDepth = Integer.parseInt(props.getProperty("crawler.maxDepth", "2"));
            String userAgent = props.getProperty("crawler.userAgent", "PotatorCrawler/1.0");
            long politenessMs = Long.parseLong(props.getProperty("crawler.politenessMs", "500"));
            String allowedHostsFile = props.getProperty("crawler.allowedHostsFile", null);

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

            return new CrawlerConfig(maxPages, maxDepth, userAgent, politenessMs, allowedHosts);

        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration", e);
        }
    }
}
