package com.potato.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashSet;
import java.util.Set;

public class HtmlParser {

    /**
     * Parse the raw HTML into a Jsoup Document, extract the page title
     * and all absolute links (<a href="...">).
     *
     * @param html    the HTML source we fetched
     * @param baseUrl the URL we fetched it from (used by Jsoup to resolve relative links)
     * @return a simple ParsedPage with title + unique links
     */
    public ParsedPage parse(String html, String baseUrl) {
        Document doc = Jsoup.parse(html, baseUrl);

        String title = doc.title();
        Set<String> links = new HashSet<>();

        Elements anchors = doc.select("a[href]");
        for (Element a : anchors) {
            String abs = a.attr("abs:href");
            if (abs != null && !abs.isBlank()) {
                links.add(abs.split("#")[0]);
            }
        }
        return new ParsedPage(title, links);
    }

    public static class ParsedPage {
        public final String title;
        public final Set<String> links;

        public ParsedPage(String title, Set<String> links) {
            this.title = title;
            this.links = links;
        }
    }
}