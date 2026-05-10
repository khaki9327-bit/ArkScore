package com.arkscore.walletdata;

import com.arkscore.solana.SolanaRpcProperties;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class WalletDataCache {

    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final SolanaRpcProperties properties;
    private final Clock clock;

    public WalletDataCache(SolanaRpcProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    public Optional<WalletData> get(String walletAddress) {
        CacheEntry entry = cache.get(walletAddress);

        if (entry == null) {
            return Optional.empty();
        }

        if (!Instant.now(clock).isBefore(entry.expiresAt())) {
            cache.remove(walletAddress, entry);
            return Optional.empty();
        }

        return Optional.of(entry.walletData());
    }

    public void put(String walletAddress, WalletData walletData) {
        cache.put(
                walletAddress,
                new CacheEntry(walletData, Instant.now(clock).plus(properties.getWalletCacheTtl()))
        );
    }

    private record CacheEntry(
            WalletData walletData,
            Instant expiresAt
    ) {
    }
}
