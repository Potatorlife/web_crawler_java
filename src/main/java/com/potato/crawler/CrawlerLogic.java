package main.java.com.potato.crawler;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import main.java.com.potato.config.CrawlerConfig;
import main.java.com.potato.fetcher.PageFetcher;
import main.java.com.potato.fetcher.PageFetcher.FetchResult;
import main.java.com.potato.parser.HtmlParser;
import main.java.com.potato.util.HostRateLimiter;
import main.java.com.potato.util.UrlUtils;
import main.java.com.potato.robots.RobotsCache;
import main.java.com.potato.robots.RobotsFetcher;
import main.java.com.potato.robots.RobotsRules;
import main.java.com.potato.storage.FilePageStorage;
import main.java.com.potato.storage.PageStorage;

public class CrawlerLogic {

    private final CrawlerConfig config;
    private final PageFetcher fetcher;
    private final HtmlParser parser;
    private final HostRateLimiter rateLimiter;
    private final RobotsCache robotsCache;
    private final PageStorage storage;

    private final PriorityBlockingQueue<CrawlItem> frontier = new PriorityBlockingQueue<>();
    private final Set<String> visited = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final AtomicInteger pagesCrawled = new AtomicInteger(0);
    private final ExecutorService executor;
    private volatile boolean running = true;
    private final AtomicInteger sequence = new AtomicInteger(0);

    public CrawlerLogic(CrawlerConfig config) {
        this.config = config;
        this.fetcher = new PageFetcher(config.getUserAgent());
        this.parser = new HtmlParser();
        this.rateLimiter = new HostRateLimiter(config.getPolitenessMs());
        this.robotsCache = new RobotsCache(new RobotsFetcher(config.getUserAgent()));
        try {
            this.storage = new FilePageStorage("data");
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize storage", e);
        }
        this.executor = Executors.newFixedThreadPool(4);
    }

    public void crawl(List<String> seedUrls) {
        for (String seed : seedUrls) {
            String norm = UrlUtils.normalize(seed);
            if (isHostAllowed(norm)) {
                frontier.add(new CrawlItem(norm, 0, sequence.getAndIncrement()));
            }
        }

        for (int i = 0; i < 4; i++) {
            executor.submit(new Worker());
        }

        try {
            while (running) {
                if (pagesCrawled.get() >= config.getMaxPages()) {
                    running = false;
                    break;
                }
                if (frontier.isEmpty()) {
                    Thread.sleep(500);
                    if (frontier.isEmpty()) {
                        running = false;
                        break;
                    }
                } else {
                    Thread.sleep(200);
                }
            }
        } catch (InterruptedException ignored) {
        } finally {
            executor.shutdownNow();
        }
    }

    private boolean isHostAllowed(String url) {
        if (!config.hasHostWhitelist()) {
            return true; 
        }
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            if (host == null) return false;
            return config.getAllowedHosts().contains(host.toLowerCase());
        } catch (Exception e) {
            return false;
        }
    }

    private class Worker implements Runnable {
        @Override
        public void run() {
            while (running && !Thread.currentThread().isInterrupted()) {
                try {
                    CrawlItem current = frontier.poll(1, TimeUnit.SECONDS);
                    if (current == null) {
                        continue;
                    }

                    String currentUrl = UrlUtils.normalize(current.url);

                    // domain whitelist check
                    if (!isHostAllowed(currentUrl)) {
                        continue;
                    }

                    if (visited.contains(currentUrl)) {
                        continue;
                    }

                    if (current.depth > config.getMaxDepth()) {
                        continue;
                    }

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

                    String host = uri.getHost();
                    if (host != null) {
                        rateLimiter.acquire(host);
                    }

                    FetchResult result = fetcher.fetch(currentUrl);

                    if (result.statusCode != 200) {
                        System.out.println("Failed " + currentUrl + " status: " + result.statusCode);
                        continue;
                    }

                    if (result.contentType == null ||
                            !result.contentType.toLowerCase().contains("text/html")) {
                        System.out.println("Skip non-HTML: " + currentUrl + " (" + result.contentType + ")");
                        continue;
                    }

                    long maxSize = 1_000_000;
                    if (result.contentLength > 0 && result.contentLength > maxSize) {
                        System.out.println("Skip too large: " + currentUrl + " (" + result.contentLength + " bytes)");
                        continue;
                    }

                    HtmlParser.ParsedPage page = parser.parse(result.body, currentUrl);

                    try {
                        storage.save(currentUrl, page.title, result.body, page.links);
                    } catch (IOException io) {
                        System.out.println("Failed to store page " + currentUrl + " : " + io.getMessage());
                    }

                    int num = pagesCrawled.incrementAndGet();
                    visited.add(currentUrl);

                    System.out.printf("(%d) [%d] %s -> %s%n",
                            num,
                            current.depth,
                            page.title,
                            currentUrl);

                    if (num >= config.getMaxPages()) {
                        running = false;
                    }

                    // enqueue children (but only allowed hosts)
                    for (String link : page.links) {
                        String normLink = UrlUtils.normalize(link);
                        if (isHostAllowed(normLink) && !visited.contains(normLink)) {
                            frontier.add(new CrawlItem(
                                    normLink,
                                    current.depth + 1,
                                    sequence.getAndIncrement()
                            ));
                        }
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    System.out.println("Worker error: " + e.getMessage());
                }
            }
        }
    }

    private static class CrawlItem implements Comparable<CrawlItem> {
        final String url;
        final int depth;
        final int seq;

        CrawlItem(String url, int depth, int seq) {
            this.url = url;
            this.depth = depth;
            this.seq = seq;
        }

        @Override
        public int compareTo(CrawlItem other) {
            int c = Integer.compare(this.depth, other.depth);
            if (c != 0) return c;
            return Integer.compare(this.seq, other.seq);
        }
    }
}
