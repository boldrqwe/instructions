package com.example.instructions.upload;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Простейший in-memory rate limiter для загрузок.
 */
@Component
public class UploadRateLimiter {

    private static final int LIMIT = 20;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final Map<String, Deque<Instant>> buckets = new ConcurrentHashMap<>();

    public boolean tryAcquire(String key) {
        Instant now = Instant.now();
        Deque<Instant> deque = buckets.computeIfAbsent(key, it -> new ArrayDeque<>());
        synchronized (deque) {
            while (!deque.isEmpty() && Duration.between(deque.peekFirst(), now).compareTo(WINDOW) > 0) {
                deque.pollFirst();
            }
            if (deque.size() >= LIMIT) {
                return false;
            }
            deque.addLast(now);
            return true;
        }
    }
}
