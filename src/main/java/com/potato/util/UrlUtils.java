package com.potato.util;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * URL helper methods used by the crawler.
 * Right now we only have a simple "normalize" to make URLs comparable.
 */
public class UrlUtils {

    /**
     * Normalize a URL so we store it in a consistent form.
     * Things we do here:
     * - default missing scheme to https
     * - lowercase the host
     * - ensure there is at least "/" as a path
     * - keep the query string if present
     *
     * @param url original URL (possibly messy)
     * @return normalized URL or the original input if parsing failed
     */
    public static String normalize(String url) {
        if (url == null || url.isBlank()) {
            return url;
        }

        try {
            URI uri = new URI(url);

            String scheme = uri.getScheme();
            String host = uri.getHost();
            String path = uri.getPath();
            String query = uri.getQuery();

            if (scheme == null) {
                scheme = "https";
            }

            if (host != null) {
                host = host.toLowerCase();
            }

            if (path == null || path.isEmpty()) {
                path = "/";
            }

            StringBuilder sb = new StringBuilder();
            sb.append(scheme).append("://").append(host).append(path);

            if (query != null && !query.isEmpty()) {
                sb.append("?").append(query);
            }

            return sb.toString();
        } catch (URISyntaxException e) {
            return url;
        }
    }
}
