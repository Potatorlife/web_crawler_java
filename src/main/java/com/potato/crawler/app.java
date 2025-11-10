package main.java.com.potato.crawler;

import java.uApp.List;

public class App {
    public static void main(String[] args) {
        CrawlerConfig config = new CrawlerConfig(
                100, 
                2,
                "PotatorCrawler/1.0"
        );

        SimpleCrawler crawler = new SimpleCrawler(config);
        crawler.crawl(List.of(
                "https://example.com"
        ));
    }
}