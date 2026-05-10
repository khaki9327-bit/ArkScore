package com.arkscore.solana;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

class SolanaRpcPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(PropertiesConfig.class);

    @Test
    void defaultsAreAppliedWhenValuesAreNotConfigured() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();

            SolanaRpcProperties properties = context.getBean(SolanaRpcProperties.class);

            assertThat(properties.getRpcUrl()).isEqualTo("https://api.mainnet-beta.solana.com");
            assertThat(properties.getTransactionLimit()).isEqualTo(20);
            assertThat(properties.getTransactionDetailLimit()).isEqualTo(3);
            assertThat(properties.getCommitment()).isEqualTo("finalized");
            assertThat(properties.getWalletCacheTtl()).isEqualTo(Duration.ofMinutes(15));
            assertThat(properties.getRequestMinInterval()).isEqualTo(Duration.ofMillis(800));
            assertThat(properties.getRateLimitRetryMaxAttempts()).isEqualTo(2);
            assertThat(properties.getRateLimitRetryBackoff()).isEqualTo(Duration.ofSeconds(3));
            assertThat(properties.getMaxConcurrentRequests()).isEqualTo(1);
            assertThat(properties.isAllowPartialTransactionDetails()).isTrue();
        });
    }

    @Test
    void valuesCanBeConfigured() {
        contextRunner
                .withPropertyValues(
                        "arkscore.solana.rpc-url=https://solana.test",
                        "arkscore.solana.transaction-limit=12",
                        "arkscore.solana.transaction-detail-limit=4",
                        "arkscore.solana.commitment=confirmed",
                        "arkscore.solana.wallet-cache-ttl=PT2M",
                        "arkscore.solana.request-min-interval=125ms",
                        "arkscore.solana.rate-limit-retry-max-attempts=4",
                        "arkscore.solana.rate-limit-retry-backoff=250ms",
                        "arkscore.solana.max-concurrent-requests=2",
                        "arkscore.solana.allow-partial-transaction-details=false"
                )
                .run(context -> {
                    assertThat(context).hasNotFailed();

                    SolanaRpcProperties properties = context.getBean(SolanaRpcProperties.class);

                    assertThat(properties.getRpcUrl()).isEqualTo("https://solana.test");
                    assertThat(properties.getTransactionLimit()).isEqualTo(12);
                    assertThat(properties.getTransactionDetailLimit()).isEqualTo(4);
                    assertThat(properties.getCommitment()).isEqualTo("confirmed");
                    assertThat(properties.getWalletCacheTtl()).isEqualTo(Duration.ofMinutes(2));
                    assertThat(properties.getRequestMinInterval()).isEqualTo(Duration.ofMillis(125));
                    assertThat(properties.getRateLimitRetryMaxAttempts()).isEqualTo(4);
                    assertThat(properties.getRateLimitRetryBackoff()).isEqualTo(Duration.ofMillis(250));
                    assertThat(properties.getMaxConcurrentRequests()).isEqualTo(2);
                    assertThat(properties.isAllowPartialTransactionDetails()).isFalse();
                });
    }

    @Test
    void transactionLimitCannotExceedTwenty() {
        contextRunner
                .withPropertyValues("arkscore.solana.transaction-limit=21")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void transactionDetailLimitCannotExceedTwenty() {
        contextRunner
                .withPropertyValues("arkscore.solana.transaction-detail-limit=21")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void rateLimitRetryMaxAttemptsMustBePositive() {
        contextRunner
                .withPropertyValues("arkscore.solana.rate-limit-retry-max-attempts=0")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void maxConcurrentRequestsMustBePositive() {
        contextRunner
                .withPropertyValues("arkscore.solana.max-concurrent-requests=0")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void invalidCommitmentFailsStartup() {
        contextRunner
                .withPropertyValues("arkscore.solana.commitment=latest")
                .run(context -> assertThat(context).hasFailed());
    }

    @Configuration
    @EnableConfigurationProperties(SolanaRpcProperties.class)
    static class PropertiesConfig {
    }
}
