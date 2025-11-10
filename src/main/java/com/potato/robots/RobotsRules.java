package com.potato.robots;

import java.util.ArrayList;
import java.util.List;

public class RobotsRules {
    private final List<String> disallows = new ArrayList<>();
    private final List<String> sitemaps = new ArrayList<>();

    public void addDisallow(String path) {
        disallows.add(path);
    }

    public boolean isAllowed(String path) {
        for (String d : disallows) {
            if (path.startsWith(d)) {
                return false;
            }
        }
        return true;
    }

    public void addSitemap(String url) {
        sitemaps.add(url);
    }

    public List<String> getSitemaps() {
        return sitemaps;
    }
}
