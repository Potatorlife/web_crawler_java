package com.potato.sitemap;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class SitemapFetcher {

    private final String userAgent;

    public SitemapFetcher(String userAgent) {
        this.userAgent = userAgent;
    }

    public List<String> fetch(String sitemapUrl) {
        List<String> urls = new ArrayList<>();
        try {
            URL url = URI.create(sitemapUrl).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", userAgent);
            conn.setConnectTimeout(4000);
            conn.setReadTimeout(4000);

            int code = conn.getResponseCode();
            if (code != 200) {
                return urls;
            }

            try (InputStream in = conn.getInputStream()) {
                Document doc = DocumentBuilderFactory.newInstance()
                        .newDocumentBuilder()
                        .parse(in);
                doc.getDocumentElement().normalize();

                NodeList locs = doc.getElementsByTagName("loc");
                for (int i = 0; i < locs.getLength(); i++) {
                    String loc = locs.item(i).getTextContent().trim();
                    if (!loc.isEmpty()) {
                        urls.add(loc);
                    }
                }
            }

        } catch (Exception ignored) {
        }
        return urls;
    }
}
