package com.potato.fetcher;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.zip.GZIPInputStream;

public class PageFetcher {

    private final HttpClient client;
    private final String userAgent;
    private final int maxRetries;
    private final long backoffMs;

    public PageFetcher(String userAgent) {
        this(userAgent, 3, 500, null);
    }

    public PageFetcher(String userAgent,
                       int maxRetries,
                       long backoffMs,
                       ProxySelector proxySelector) {

        HttpClient.Builder builder = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(5));

        if (proxySelector != null) {
            builder.proxy(proxySelector);
        }

        this.client = builder.build();
        this.userAgent = userAgent;
        this.maxRetries = maxRetries;
        this.backoffMs = backoffMs;
    }

    public FetchResult fetch(String url) throws IOException, InterruptedException {
        IOException lastIo = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("User-Agent", userAgent)
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                        .header("Accept-Encoding", "gzip")
                        .GET()
                        .build();

                HttpResponse<byte[]> response =
                        client.send(request, HttpResponse.BodyHandlers.ofByteArray());

                int status = response.statusCode();

                String contentType = response.headers()
                        .firstValue("Content-Type").orElse(null);

                long contentLength = response.headers()
                        .firstValueAsLong("Content-Length").orElse(-1L);

                byte[] bodyBytes = response.body();
                String encoding = response.headers().firstValue("Content-Encoding").orElse("");
                if ("gzip".equalsIgnoreCase(encoding)) {
                    bodyBytes = ungzip(bodyBytes);
                }

                String body = new String(bodyBytes, java.nio.charset.StandardCharsets.UTF_8);

                return new FetchResult(
                        status,
                        body,
                        url,
                        contentType,
                        contentLength
                );

            } catch (IOException e) {
                lastIo = e;
                Thread.sleep(backoffMs * attempt);
            }
        }

        throw lastIo != null ? lastIo : new IOException("Failed to fetch " + url);
    }

    private byte[] ungzip(byte[] gzipped) throws IOException {
        try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(gzipped))) {
            return gis.readAllBytes();
        }
    }

    public static class FetchResult {
        public final int statusCode;
        public final String body;
        public final String url;
        public final String contentType;
        public final long contentLength;

        public FetchResult(int statusCode,
                           String body,
                           String url,
                           String contentType,
                           long contentLength) {
            this.statusCode = statusCode;
            this.body = body;
            this.url = url;
            this.contentType = contentType;
            this.contentLength = contentLength;
        }
    }
}
