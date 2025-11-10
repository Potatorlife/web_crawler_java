package main.java.com.potato.crawler;

import java.util.List;

import main.java.com.potato.config.ConfigLoader;
import main.java.com.potato.config.CrawlerConfig;

public class App {
    public static void main(String[] args) {
        CrawlerConfig config = ConfigLoader.load();
        CrawlerLogic crawler = new CrawlerLogic(config);
        crawler.crawl(List.of("https://example.com"));
    }
}
