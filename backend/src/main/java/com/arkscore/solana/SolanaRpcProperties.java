package com.arkscore.solana;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.Duration;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "arkscore.solana")
public class SolanaRpcProperties {

    @NotBlank(message = "Solana RPC URL is required.")
    private String rpcUrl = "https://api.mainnet-beta.solana.com";

    @Min(1)
    @Max(20)
    private int transactionLimit = 20;

    @Min(0)
    @Max(20)
    private int transactionDetailLimit = 3;

    @NotBlank(message = "Solana commitment is required.")
    @Pattern(regexp = "processed|confirmed|finalized", message = "Solana commitment must be processed, confirmed, or finalized.")
    private String commitment = "finalized";

    @NotNull
    private Duration walletCacheTtl = Duration.ofMinutes(15);

    @NotNull
    @DurationMin(millis = 0)
    private Duration requestMinInterval = Duration.ofMillis(800);

    @Min(1)
    @Max(10)
    private int rateLimitRetryMaxAttempts = 2;

    @NotNull
    @DurationMin(millis = 1)
    private Duration rateLimitRetryBackoff = Duration.ofSeconds(3);

    @Min(1)
    @Max(40)
    private int maxConcurrentRequests = 1;

    private boolean allowPartialTransactionDetails = true;

    public String getRpcUrl() {
        return rpcUrl;
    }

    public void setRpcUrl(String rpcUrl) {
        this.rpcUrl = rpcUrl;
    }

    public int getTransactionLimit() {
        return transactionLimit;
    }

    public void setTransactionLimit(int transactionLimit) {
        this.transactionLimit = transactionLimit;
    }

    public int getTransactionDetailLimit() {
        return transactionDetailLimit;
    }

    public void setTransactionDetailLimit(int transactionDetailLimit) {
        this.transactionDetailLimit = transactionDetailLimit;
    }

    public String getCommitment() {
        return commitment;
    }

    public void setCommitment(String commitment) {
        this.commitment = commitment;
    }

    public Duration getWalletCacheTtl() {
        return walletCacheTtl;
    }

    public void setWalletCacheTtl(Duration walletCacheTtl) {
        this.walletCacheTtl = walletCacheTtl;
    }

    public Duration getRequestMinInterval() {
        return requestMinInterval;
    }

    public void setRequestMinInterval(Duration requestMinInterval) {
        this.requestMinInterval = requestMinInterval;
    }

    public int getRateLimitRetryMaxAttempts() {
        return rateLimitRetryMaxAttempts;
    }

    public void setRateLimitRetryMaxAttempts(int rateLimitRetryMaxAttempts) {
        this.rateLimitRetryMaxAttempts = rateLimitRetryMaxAttempts;
    }

    public Duration getRateLimitRetryBackoff() {
        return rateLimitRetryBackoff;
    }

    public void setRateLimitRetryBackoff(Duration rateLimitRetryBackoff) {
        this.rateLimitRetryBackoff = rateLimitRetryBackoff;
    }

    public int getMaxConcurrentRequests() {
        return maxConcurrentRequests;
    }

    public void setMaxConcurrentRequests(int maxConcurrentRequests) {
        this.maxConcurrentRequests = maxConcurrentRequests;
    }

    public boolean isAllowPartialTransactionDetails() {
        return allowPartialTransactionDetails;
    }

    public void setAllowPartialTransactionDetails(boolean allowPartialTransactionDetails) {
        this.allowPartialTransactionDetails = allowPartialTransactionDetails;
    }
}
