package main.java.com.potato.crawler;

import java.util.List;

import main.java.com.potato.config.CrawlerConfig;

public class App {
    public static void main(String[] args) {

        CrawlerConfig config = new CrawlerConfig(
                100,
                2,
                "PotatorCrawler/1.0"
        );

        CrawlerLogic crawler = new CrawlerLogic(config);

        crawler.crawl(List.of(
                "https://example.com"
        ));
    }
};