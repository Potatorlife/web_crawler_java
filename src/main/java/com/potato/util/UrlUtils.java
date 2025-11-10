package com.potato.util;

import java.net.URI;
import java.net.URISyntaxException;

public class UrlUtils {

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
