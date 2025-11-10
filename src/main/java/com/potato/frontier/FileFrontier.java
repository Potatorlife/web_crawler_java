package com.potato.frontier;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class FileFrontier implements Frontier {

    private final File file;
    private final BlockingQueue<Prioritized> queue = new PriorityBlockingQueue<>();
    private final AtomicInteger seq = new AtomicInteger(0);
    private final BufferedWriter writer;

    public FileFrontier(String path) throws IOException {
        this.file = new File(path);
        // load existing
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(" ", 2);
                    int depth = Integer.parseInt(parts[0]);
                    String url = parts[1];
                    queue.add(new Prioritized(url, depth, seq.getAndIncrement()));
                }
            }
        }
        this.writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8, true));
    }

    @Override
    public void add(String url, int depth) {
        queue.add(new Prioritized(url, depth, seq.getAndIncrement()));
        try {
            writer.write(depth + " " + url);
            writer.newLine();
            writer.flush();
        } catch (IOException ignored) {}
    }

    @Override
    public CrawlTask poll(long timeoutMs) throws InterruptedException {
        Prioritized p = queue.poll();
        if (p == null) return null;
        return new CrawlTask(p.url, p.depth);
    }

    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    public void close() {
        try {
            writer.close();
        } catch (IOException ignored) {}
    }

    private static class Prioritized implements Comparable<Prioritized> {
        final String url;
        final int depth;
        final int seq;

        Prioritized(String url, int depth, int seq) {
            this.url = url;
            this.depth = depth;
            this.seq = seq;
        }

        @Override
        public int compareTo(Prioritized other) {
            int c = Integer.compare(this.depth, other.depth);
            if (c != 0) return c;
            return Integer.compare(this.seq, other.seq);
        }
    }
}
