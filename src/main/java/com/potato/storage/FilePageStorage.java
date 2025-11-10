package com.potato.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

/**
 * Simple file-based page storage.
 *
 * For each fetched page we:
 * 1) create a stable ID from the URL (SHA-1)
 * 2) store the raw HTML in <id>.html
 * 3) store a small JSON metadata file in <id>.json (url, title, links)
 *
 * This makes it easy to inspect what the crawler fetched.
 */
public class FilePageStorage implements PageStorage {

    private final Path baseDir;

    public FilePageStorage(String baseDir) throws IOException {
        this.baseDir = Paths.get(baseDir);
        Files.createDirectories(this.baseDir);
    }

    @Override
    public void save(String url,
                     String title,
                     String html,
                     Collection<String> links) throws IOException {

        String id = sha1(url);

        Path htmlFile = baseDir.resolve(id + ".html");
        Files.writeString(htmlFile, html, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);

        Path jsonFile = baseDir.resolve(id + ".json");
        String json = buildJson(url, title, links);
        Files.writeString(jsonFile, json, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }

    /**
     * Build a very simple JSON object with url, title and links.
     * We hand-roll it to avoid bringing in a JSON library.
     */
    private String buildJson(String url, String title, Collection<String> links) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"url\": ").append(quote(url)).append(",\n");
        sb.append("  \"title\": ").append(quote(title)).append(",\n");
        sb.append("  \"links\": [");
        boolean first = true;
        for (String l : links) {
            if (!first) sb.append(", ");
            sb.append(quote(l));
            first = false;
        }
        sb.append("]\n");
        sb.append("}\n");
        return sb.toString();
    }

     /**
     * Escape quotes and backslashes for JSON strings.
     */
    private String quote(String s) {
        if (s == null) return "null";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    /**
     * Create a deterministic ID for a URL using SHA-1.
     * If SHA-1 is not available (very rare), fall back to hashCode.
     */
    private String sha1(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return Integer.toHexString(input.hashCode());
        }
    }
}
