package main.java.com.potato.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashSet;
import java.util.Set;

public class HtmlParser {

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