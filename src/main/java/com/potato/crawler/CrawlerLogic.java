package main.java.com.potato.crawler;

import java.io.IOException;
import java.net.URI;
import java.util.*;

import main.java.com.potato.config.CrawlerConfig;
import main.java.com.potato.fetcher.PageFetcher;
import main.java.com.potato.parser.HtmlParser;
import main.java.com.potato.util.HostRateLimiter;
import main.java.com.potato.util.UrlUtils;
import main.java.com.potato.robots.RobotsCache;
import main.java.com.potato.robots.RobotsFetcher;
import main.java.com.potato.robots.RobotsRules;

public class CrawlerLogic {

    private final CrawlerConfig config;
    private final PageFetcher fetcher;
    private final HtmlParser parser;
    private final HostRateLimiter rateLimiter;
    private final RobotsCache robotsCache;

    public CrawlerLogic(CrawlerConfig config) {
        this.config = config;
        this.fetcher = new PageFetcher(config.getUserAgent());
        this.parser = new HtmlParser();
        this.rateLimiter = new HostRateLimiter(500);
        this.robotsCache = new RobotsCache(new RobotsFetcher(config.getUserAgent()));
    }

    public void crawl(List<String> seedUrls) {
        Queue<CrawlItem> frontier = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();

        // normalize seeds
        for (String seed : seedUrls) {
            String norm = UrlUtils.normalize(seed);
            frontier.add(new CrawlItem(norm, 0));
        }

        int pagesCrawled = 0;

        while (!frontier.isEmpty() && pagesCrawled < config.getMaxPages()) {
            CrawlItem current = frontier.poll();

            // normalize again just in case
            String currentUrl = UrlUtils.normalize(current.url);

            if (visited.contains(currentUrl)) {
                continue;
            }

            if (current.depth > config.getMaxDepth()) {
                continue;
            }

            try {
                // robots.txt
                RobotsRules rules = robotsCache.getRulesFor(currentUrl);
                URI uri = new URI(currentUrl);
                String path = uri.getRawPath();
                if (path == null || path.isEmpty()) {
                    path = "/";
                }
                if (!rules.isAllowed(path)) {
                    System.out.println("Blocked by robots.txt: " + currentUrl);
                    continue;
                }

                // politeness
                String host = uri.getHost();
                if (host != null) {
                    rateLimiter.acquire(host);
                }

                PageFetcher.FetchResult result = fetcher.fetch(currentUrl);
                if (result.statusCode == 200) {
                    HtmlParser.ParsedPage page = parser.parse(result.body, currentUrl);

                    System.out.printf("(%d) [%d] %s -> %s%n",
                            pagesCrawled + 1,
                            current.depth,
                            page.title,
                            currentUrl);

                    visited.add(currentUrl);
                    pagesCrawled++;

                    // enqueue discovered links
                    for (String link : page.links) {
                        String normLink = UrlUtils.normalize(link);
                        if (!visited.contains(normLink)) {
                            frontier.add(new CrawlItem(normLink, current.depth + 1));
                        }
                    }
                } else {
                    System.out.println("Failed " + currentUrl + " status: " + result.statusCode);
                }

            } catch (IOException | InterruptedException e) {
                System.out.println("Error fetching " + current.url + " : " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Error processing " + current.url + " : " + e.getMessage());
            }
        }
    }

    private static class CrawlItem {
        String url;
        int depth;

        CrawlItem(String url, int depth) {
            this.url = url;
            this.depth = depth;
        }
    }
}
