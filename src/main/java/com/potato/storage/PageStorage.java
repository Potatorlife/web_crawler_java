package com.potato.storage;

import java.io.IOException;
import java.util.Collection;

public interface PageStorage {
    void save(String url,
              String title,
              String html,
              Collection<String> links) throws IOException;
}
