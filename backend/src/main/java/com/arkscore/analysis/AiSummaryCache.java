package com.arkscore.analysis;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class AiSummaryCache {

    private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();

    public Optional<String> get(String key) {
        return Optional.ofNullable(cache.get(key));
    }

    public void put(String key, String summary) {
        cache.put(key, summary);
    }
}
