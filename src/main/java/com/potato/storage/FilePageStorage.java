package main.java.com.potato.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

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

    private String quote(String s) {
        if (s == null) return "null";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

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
