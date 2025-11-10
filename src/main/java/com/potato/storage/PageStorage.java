package com.potato.storage;

import java.io.IOException;
import java.util.Collection;

/**
 * Abstraction for storing fetched pages.
 * Different implementations can store to files, databases, cloud storage, etc.
 */
public interface PageStorage {
    void save(String url,
              String title,
              String html,
              Collection<String> links) throws IOException;
}
