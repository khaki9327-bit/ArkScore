package com.arkscore.solana;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.arkscore.walletdata.WalletData;
import com.arkscore.walletdata.WalletDataCache;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SolanaWalletDataProviderTest {

    private MutableClock clock;
    private FakeSolanaRpcClient rpcClient;
    private SolanaRpcProperties properties;
    private SolanaWalletDataProvider provider;

    @BeforeEach
    void setUp() {
        properties = new SolanaRpcProperties();
        properties.setTransactionLimit(2);
        properties.setTransactionDetailLimit(2);
        properties.setWalletCacheTtl(Duration.ofMinutes(5));
        clock = new MutableClock(Instant.parse("2026-05-09T00:00:00Z"));
        rpcClient = new FakeSolanaRpcClient();
        provider = new SolanaWalletDataProvider(
                rpcClient,
                properties,
                new WalletDataCache(properties, clock)
        );
    }

    @Test
    void sameWalletUsesCacheWithinTtl() {
        WalletData first = provider.getWalletData("wallet123");
        WalletData second = provider.getWalletData("wallet123");

        assertThat(second).isSameAs(first);
        assertThat(rpcClient.balanceCalls).isEqualTo(1);
        assertThat(rpcClient.signatureLimits).containsExactly(2);
        assertThat(rpcClient.transactionDetailCalls).isEqualTo(2);
    }

    @Test
    void onlyFetchesConfiguredTransactionDetailLimit() {
        properties.setTransactionDetailLimit(1);

        WalletData walletData = provider.getWalletData("wallet123");

        assertThat(walletData.recentTransactions()).hasSize(2);
        assertThat(walletData.transactionDetails())
                .extracting(WalletData.TransactionDetails::signature)
                .containsExactly("wallet123-sig1");
        assertThat(rpcClient.transactionDetailSignatures)
                .containsExactly("wallet123-sig1");
    }

    @Test
    void defaultOnlyFetchesThreeTransactionDetails() {
        SolanaRpcProperties defaultProperties = new SolanaRpcProperties();
        defaultProperties.setTransactionLimit(5);
        defaultProperties.setWalletCacheTtl(Duration.ofMinutes(5));
        FakeSolanaRpcClient defaultRpcClient = new FakeSolanaRpcClient();
        defaultRpcClient.recentTransactionCount = 5;
        SolanaWalletDataProvider defaultProvider = new SolanaWalletDataProvider(
                defaultRpcClient,
                defaultProperties,
                new WalletDataCache(defaultProperties, clock)
        );

        WalletData walletData = defaultProvider.getWalletData("wallet123");

        assertThat(walletData.recentTransactions()).hasSize(5);
        assertThat(walletData.transactionDetails())
                .extracting(WalletData.TransactionDetails::signature)
                .containsExactly("wallet123-sig1", "wallet123-sig2", "wallet123-sig3");
        assertThat(defaultRpcClient.transactionDetailCalls).isEqualTo(3);
    }

    @Test
    void cachesPartialTransactionDetailsWhenRateLimited() {
        rpcClient.rateLimitedTransactionDetailSignature = "wallet123-sig2";

        WalletData first = provider.getWalletData("wallet123");
        WalletData second = provider.getWalletData("wallet123");

        assertThat(second).isSameAs(first);
        assertThat(first.transactionDetails())
                .extracting(WalletData.TransactionDetails::signature)
                .containsExactly("wallet123-sig1");
        assertThat(rpcClient.transactionDetailSignatures)
                .containsExactly("wallet123-sig1", "wallet123-sig2");
        assertThat(rpcClient.transactionDetailCalls).isEqualTo(2);
    }

    @Test
    void cacheExpiresAfterTtl() {
        provider.getWalletData("wallet123");
        clock.advance(Duration.ofMinutes(6));

        provider.getWalletData("wallet123");

        assertThat(rpcClient.balanceCalls).isEqualTo(2);
    }

    @Test
    void differentWalletUsesDifferentCacheKey() {
        provider.getWalletData("wallet123");
        provider.getWalletData("wallet456");

        assertThat(rpcClient.balanceCalls).isEqualTo(2);
    }

    @Test
    void rpcFailureIsNotCached() {
        rpcClient.failBalance = true;

        assertThatThrownBy(() -> provider.getWalletData("wallet123"))
                .isInstanceOf(SolanaRpcClientException.class);

        rpcClient.failBalance = false;

        provider.getWalletData("wallet123");

        assertThat(rpcClient.balanceCalls).isEqualTo(2);
    }

    private static class FakeSolanaRpcClient implements SolanaRpcOperations {

        private int balanceCalls;
        private int transactionDetailCalls;
        private int recentTransactionCount = 2;
        private boolean failBalance;
        private String rateLimitedTransactionDetailSignature;
        private final List<Integer> signatureLimits = new ArrayList<>();
        private final List<String> transactionDetailSignatures = new ArrayList<>();

        @Override
        public long getBalanceLamports(String walletAddress) {
            balanceCalls++;

            if (failBalance) {
                throw new SolanaRpcClientException("Wallet data provider failed.");
            }

            return 1_000_000_000L;
        }

        @Override
        public List<WalletData.TokenHolding> getTokenAccountsByOwner(String walletAddress) {
            return List.of(new WalletData.TokenHolding("token1", "MintA", "100", 6));
        }

        @Override
        public List<WalletData.RecentTransaction> getSignaturesForAddress(String walletAddress, int limit) {
            signatureLimits.add(limit);
            List<WalletData.RecentTransaction> transactions = new ArrayList<>();
            for (int index = 1; index <= Math.min(limit, recentTransactionCount); index++) {
                transactions.add(new WalletData.RecentTransaction(
                        walletAddress + "-sig" + index,
                        index,
                        1_700_000_000L + index,
                        false
                ));
            }

            return transactions;
        }

        @Override
        public WalletData.TransactionDetails getTransactionDetails(String signature) {
            transactionDetailCalls++;
            transactionDetailSignatures.add(signature);

            if (signature.equals(rateLimitedTransactionDetailSignature)) {
                throw new SolanaRpcRateLimitException("Wallet data provider failed.");
            }

            return new WalletData.TransactionDetails(signature, List.of("wallet123", "account1"));
        }
    }

    private static class MutableClock extends Clock {

        private Instant instant;

        MutableClock(Instant instant) {
            this.instant = instant;
        }

        void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
