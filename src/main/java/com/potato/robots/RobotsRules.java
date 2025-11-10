package com.potato.robots;

import java.util.ArrayList;
import java.util.List;

public class RobotsRules {

    private final List<String> disallows = new ArrayList<>();

    public void addDisallow(String path) {
        disallows.add(path);
    }

    public boolean isAllowed(String path) {
        if (path == null || path.isEmpty()) {
            return true;
        }
        for (String dis : disallows) {
            if (!dis.isEmpty() && path.startsWith(dis)) {
                return false;
            }
        }
        return true;
    }
}
