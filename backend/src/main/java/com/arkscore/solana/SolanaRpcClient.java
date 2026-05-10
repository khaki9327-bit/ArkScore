package com.arkscore.solana;

import com.arkscore.walletdata.WalletData;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Service
public class SolanaRpcClient implements SolanaRpcOperations {

    private static final Logger log = LoggerFactory.getLogger(SolanaRpcClient.class);
    private static final String FAILURE_MESSAGE = "Wallet data provider failed.";
    private static final String TOKEN_PROGRAM_ID = "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA";

    private final RestClient solanaRpcHttpClient;
    private final SolanaRpcProperties properties;
    private final Semaphore requestConcurrency;
    private final AtomicLong requestId = new AtomicLong(1);
    private final Object requestPacingMonitor = new Object();
    private long nextRequestAtNanos;

    public SolanaRpcClient(
            @Qualifier("solanaRpcHttpClient") RestClient solanaRpcHttpClient,
            SolanaRpcProperties properties
    ) {
        this.solanaRpcHttpClient = solanaRpcHttpClient;
        this.properties = properties;
        this.requestConcurrency = new Semaphore(
                Math.max(1, properties.getMaxConcurrentRequests()),
                true
        );
    }

    @Override
    public long getBalanceLamports(String walletAddress) {
        JsonNode result = call("getBalance", List.of(
                walletAddress,
                commitmentConfig()
        ));
        JsonNode value = result.path("value");

        if (!value.canConvertToLong()) {
            throw new SolanaRpcClientException(FAILURE_MESSAGE);
        }

        return value.asLong();
    }

    @Override
    public List<WalletData.TokenHolding> getTokenAccountsByOwner(String walletAddress) {
        JsonNode result = call("getTokenAccountsByOwner", List.of(
                walletAddress,
                Map.of("programId", TOKEN_PROGRAM_ID),
                Map.of(
                        "encoding", "jsonParsed",
                        "commitment", properties.getCommitment()
                )
        ));
        JsonNode value = result.path("value");

        if (!value.isArray()) {
            throw new SolanaRpcClientException(FAILURE_MESSAGE);
        }

        List<WalletData.TokenHolding> tokenHoldings = new ArrayList<>();
        for (JsonNode item : value) {
            String tokenAccount = textOrNull(item.path("pubkey"));
            JsonNode info = item.path("account")
                    .path("data")
                    .path("parsed")
                    .path("info");
            String mint = textOrNull(info.path("mint"));
            JsonNode tokenAmount = info.path("tokenAmount");
            String amount = textOrNull(tokenAmount.path("amount"));
            int decimals = tokenAmount.path("decimals").canConvertToInt()
                    ? tokenAmount.path("decimals").asInt()
                    : 0;

            if (mint != null && amount != null) {
                tokenHoldings.add(new WalletData.TokenHolding(
                        tokenAccount,
                        mint,
                        amount,
                        decimals
                ));
            }
        }

        return tokenHoldings;
    }

    @Override
    public List<WalletData.RecentTransaction> getSignaturesForAddress(
            String walletAddress,
            int limit
    ) {
        JsonNode result = call("getSignaturesForAddress", List.of(
                walletAddress,
                Map.of(
                        "limit", limit,
                        "commitment", properties.getCommitment()
                )
        ));

        if (!result.isArray()) {
            throw new SolanaRpcClientException(FAILURE_MESSAGE);
        }

        List<WalletData.RecentTransaction> transactions = new ArrayList<>();
        for (JsonNode item : result) {
            String signature = textOrNull(item.path("signature"));
            JsonNode slotNode = item.path("slot");

            if (signature == null || !slotNode.canConvertToLong()) {
                throw new SolanaRpcClientException(FAILURE_MESSAGE);
            }

            Long blockTime = item.path("blockTime").canConvertToLong()
                    ? item.path("blockTime").asLong()
                    : null;

            transactions.add(new WalletData.RecentTransaction(
                    signature,
                    slotNode.asLong(),
                    blockTime,
                    !item.path("err").isMissingNode() && !item.path("err").isNull()
            ));
        }

        return transactions;
    }

    @Override
    public WalletData.TransactionDetails getTransactionDetails(String signature) {
        JsonNode result = call("getTransaction", List.of(
                signature,
                Map.of(
                        "encoding", "json",
                        "maxSupportedTransactionVersion", 0,
                        "commitment", properties.getCommitment()
                )
        ));

        if (result.isNull() || result.isMissingNode()) {
            return new WalletData.TransactionDetails(signature, List.of());
        }

        List<String> accountKeys = new ArrayList<>();
        JsonNode keys = result.path("transaction").path("message").path("accountKeys");
        if (keys.isArray()) {
            for (JsonNode key : keys) {
                String accountKey = key.isTextual()
                        ? key.asText()
                        : textOrNull(key.path("pubkey"));

                if (accountKey != null) {
                    accountKeys.add(accountKey);
                }
            }
        }

        addLoadedAddresses(accountKeys, result.path("meta").path("loadedAddresses").path("writable"));
        addLoadedAddresses(accountKeys, result.path("meta").path("loadedAddresses").path("readonly"));

        return new WalletData.TransactionDetails(signature, accountKeys);
    }

    private JsonNode call(String method, List<Object> params) {
        int maxAttempts = properties.getRateLimitRetryMaxAttempts();
        SolanaRpcRateLimitException lastRateLimitException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return callOnce(method, params, attempt);
            } catch (SolanaRpcRateLimitException exception) {
                lastRateLimitException = exception;

                if (attempt >= maxAttempts) {
                    log.error(
                            "Solana RPC rate limit retries exhausted: method={}, attempts={}",
                            method,
                            maxAttempts
                    );
                    throw exception;
                }

                Duration retryAfter = exception.getRetryAfter().orElse(null);
                Duration backoff = retryAfter == null
                        ? rateLimitRetryBackoff(attempt)
                        : retryAfter;
                log.warn(
                        "Solana RPC rate limited; retrying: method={}, attempt={}, maxAttempts={}, backoffMs={}",
                        method,
                        attempt,
                        maxAttempts,
                        backoff.toMillis()
                );
                sleep(backoff);
            }
        }

        throw lastRateLimitException == null
                ? new SolanaRpcClientException(FAILURE_MESSAGE)
                : lastRateLimitException;
    }

    private JsonNode callOnce(String method, List<Object> params, int attempt) {
        long id = requestId.getAndIncrement();
        log.debug("Solana RPC request start: method={}, attempt={}", method, attempt);

        boolean requestSlotAcquired = false;
        try {
            acquireRequestSlot();
            requestSlotAcquired = true;
            paceRequests();
            JsonNode response = solanaRpcHttpClient.post()
                    .uri("/")
                    .body(Map.of(
                            "jsonrpc", "2.0",
                            "id", id,
                            "method", method,
                            "params", params
                    ))
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null) {
                throw new SolanaRpcClientException(FAILURE_MESSAGE);
            }

            if (response.hasNonNull("error")) {
                JsonNode error = response.path("error");
                String code = error.path("code").asText("unknown");
                String message = error.path("message").asText("unknown");

                if (isRateLimitError(error)) {
                    log.warn(
                            "Solana RPC returned rate limit: method={}, code={}, message={}",
                            method,
                            code,
                            message
                    );
                    throw new SolanaRpcRateLimitException(FAILURE_MESSAGE);
                }

                log.error(
                        "Solana RPC returned error: method={}, code={}, message={}",
                        method,
                        code,
                        message
                );
                throw new SolanaRpcClientException(FAILURE_MESSAGE);
            }

            if (!response.has("result")) {
                throw new SolanaRpcClientException(FAILURE_MESSAGE);
            }

            log.debug("Solana RPC request success: method={}", method);
            return response.path("result");
        } catch (SolanaRpcClientException exception) {
            throw exception;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 429) {
                Duration retryAfter = retryAfterBackoff(exception);
                log.warn(
                        "Solana RPC HTTP rate limited: method={}, status={}, retryAfterMs={}, message={}",
                        method,
                        exception.getStatusCode().value(),
                        retryAfter == null ? null : retryAfter.toMillis(),
                        exception.getMessage()
                );
                throw new SolanaRpcRateLimitException(FAILURE_MESSAGE, exception, retryAfter);
            }

            log.error(
                    "Solana RPC request failed: method={}, message={}",
                    method,
                    exception.getMessage(),
                    exception
            );
            throw new SolanaRpcClientException(FAILURE_MESSAGE, exception);
        } catch (RestClientException exception) {
            log.error(
                    "Solana RPC request failed: method={}, message={}",
                    method,
                    exception.getMessage(),
                    exception
            );
            throw new SolanaRpcClientException(FAILURE_MESSAGE, exception);
        } finally {
            if (requestSlotAcquired) {
                requestConcurrency.release();
            }
        }
    }

    private void acquireRequestSlot() {
        try {
            requestConcurrency.acquire();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new SolanaRpcClientException(FAILURE_MESSAGE, exception);
        }
    }

    private void paceRequests() {
        Duration requestMinInterval = properties.getRequestMinInterval();
        if (requestMinInterval == null || requestMinInterval.isZero() || requestMinInterval.isNegative()) {
            return;
        }

        long requestMinIntervalNanos = requestMinInterval.toNanos();
        synchronized (requestPacingMonitor) {
            long now = System.nanoTime();
            if (now < nextRequestAtNanos) {
                sleepNanos(nextRequestAtNanos - now);
                now = System.nanoTime();
            }

            nextRequestAtNanos = now + requestMinIntervalNanos;
        }
    }

    private Duration rateLimitRetryBackoff(int failedAttempt) {
        Duration backoff = properties.getRateLimitRetryBackoff();
        if (backoff == null || backoff.isZero() || backoff.isNegative()) {
            return Duration.ZERO;
        }

        long multiplier = 1L << Math.min(failedAttempt - 1, 30);
        return backoff.multipliedBy(multiplier);
    }

    private Duration retryAfterBackoff(RestClientResponseException exception) {
        HttpHeaders headers = exception.getResponseHeaders();
        if (headers == null) {
            return null;
        }

        String retryAfter = headers.getFirst(HttpHeaders.RETRY_AFTER);
        if (retryAfter == null || retryAfter.isBlank()) {
            return null;
        }

        String trimmedRetryAfter = retryAfter.trim();
        try {
            long seconds = Long.parseLong(trimmedRetryAfter);
            return Duration.ofSeconds(Math.max(0, seconds));
        } catch (NumberFormatException ignored) {
            return retryAfterDateBackoff(trimmedRetryAfter);
        }
    }

    private Duration retryAfterDateBackoff(String retryAfter) {
        try {
            Instant retryAfterInstant = ZonedDateTime.parse(
                    retryAfter,
                    DateTimeFormatter.RFC_1123_DATE_TIME
            ).toInstant();
            Duration duration = Duration.between(Instant.now(), retryAfterInstant);
            return duration.isNegative() ? Duration.ZERO : duration;
        } catch (DateTimeParseException exception) {
            log.warn("Ignoring invalid Solana RPC Retry-After header: value={}", retryAfter);
            return null;
        }
    }

    private boolean isRateLimitError(JsonNode error) {
        String code = error.path("code").asText("");
        String message = error.path("message").asText("").toLowerCase(Locale.ROOT);

        return "429".equals(code)
                || (error.path("code").canConvertToInt() && error.path("code").asInt() == 429)
                || message.contains("too many requests")
                || message.contains("rate limit");
    }

    private void sleep(Duration duration) {
        if (duration == null || duration.isZero() || duration.isNegative()) {
            return;
        }

        sleepNanos(duration.toNanos());
    }

    private void sleepNanos(long nanos) {
        if (nanos <= 0) {
            return;
        }

        try {
            long millis = TimeUnit.NANOSECONDS.toMillis(nanos);
            int remainingNanos = (int) (nanos - TimeUnit.MILLISECONDS.toNanos(millis));
            Thread.sleep(millis, remainingNanos);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new SolanaRpcClientException(FAILURE_MESSAGE, exception);
        }
    }

    private void addLoadedAddresses(List<String> accountKeys, JsonNode loadedAddresses) {
        if (!loadedAddresses.isArray()) {
            return;
        }

        for (JsonNode loadedAddress : loadedAddresses) {
            if (loadedAddress.isTextual()) {
                accountKeys.add(loadedAddress.asText());
            }
        }
    }

    private Map<String, String> commitmentConfig() {
        return Map.of("commitment", properties.getCommitment());
    }

    private String textOrNull(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }

        String value = node.asText();
        return value.isBlank() ? null : value;
    }
}
