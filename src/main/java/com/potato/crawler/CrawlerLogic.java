package main.java.com.potato.crawler;

import java.io.IOException;
import java.util.*;

import main.java.com.potato.config.CrawlerConfig;
import main.java.com.potato.fetcher.PageFetcher;
import main.java.com.potato.parser.HtmlParser;

public class CrawlerLogic {

    private final CrawlerConfig config;
    private final PageFetcher fetcher;
    private final HtmlParser parser;

    // constructor name must match class name
    public CrawlerLogic(CrawlerConfig config) {
        this.config = config;
        this.fetcher = new PageFetcher(config.getUserAgent());
        this.parser = new HtmlParser();
    }

    public void crawl(List<String> seedUrls) {
        Queue<CrawlItem> frontier = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();

        for (String seed : seedUrls) {
            frontier.add(new CrawlItem(seed, 0));
        }

        int pagesCrawled = 0;

        while (!frontier.isEmpty() && pagesCrawled < config.getMaxPages()) {
            CrawlItem current = frontier.poll();
            if (visited.contains(current.url)) {
                continue;
            }

            if (current.depth > config.getMaxDepth()) {
                continue;
            }

            try {
                PageFetcher.FetchResult result = fetcher.fetch(current.url);
                if (result.statusCode == 200) {
                    HtmlParser.ParsedPage page = parser.parse(result.body, current.url);

                    System.out.printf("(%d) [%d] %s -> %s%n",
                            pagesCrawled + 1,
                            current.depth,
                            page.title,
                            current.url);

                    visited.add(current.url);
                    pagesCrawled++;

                    // enqueue discovered links
                    for (String link : page.links) {
                        if (!visited.contains(link)) {
                            frontier.add(new CrawlItem(link, current.depth + 1));
                        }
                    }
                } else {
                    System.out.println("Failed " + current.url + " status: " + result.statusCode);
                }

                // politeness delay
                Thread.sleep(500);

            } catch (IOException | InterruptedException e) {
                System.out.println("Error fetching " + current.url + " : " + e.getMessage());
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