package com.potato.crawler;

import java.util.List;

import com.potato.config.ConfigLoader;
import com.potato.config.CrawlerConfig;

public class App {
    public static void main(String[] args) {
        CrawlerConfig config = ConfigLoader.load();
        CrawlerLogic crawler = new CrawlerLogic(config);
        crawler.crawl(List.of("https://www.python.org"));
    }
}
