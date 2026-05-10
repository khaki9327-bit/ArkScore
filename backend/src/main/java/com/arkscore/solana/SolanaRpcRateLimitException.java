package com.arkscore.solana;

import java.time.Duration;
import java.util.Optional;

public class SolanaRpcRateLimitException extends SolanaRpcClientException {

    private final Duration retryAfter;

    public SolanaRpcRateLimitException(String message) {
        super(message);
        this.retryAfter = null;
    }

    public SolanaRpcRateLimitException(String message, Throwable cause) {
        super(message, cause);
        this.retryAfter = null;
    }

    public SolanaRpcRateLimitException(String message, Throwable cause, Duration retryAfter) {
        super(message, cause);
        this.retryAfter = retryAfter;
    }

    public Optional<Duration> getRetryAfter() {
        return Optional.ofNullable(retryAfter);
    }
}
