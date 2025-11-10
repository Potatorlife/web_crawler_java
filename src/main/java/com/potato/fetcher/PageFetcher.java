package com.potato.fetcher;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class PageFetcher {

    private final HttpClient client;
    private final String userAgent;

    public PageFetcher(String userAgent) {
        this.client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        this.userAgent = userAgent;
    }

    public FetchResult fetch(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", userAgent)
                .header("Accept", "text/html")
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        // headers
        String contentType = response.headers()
                .firstValue("Content-Type")
                .orElse(null);

        long contentLength = response.headers()
                .firstValueAsLong("Content-Length")
                .orElse(-1L);

        return new FetchResult(
                response.statusCode(),
                response.body(),
                url,
                contentType,
                contentLength
        );
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
