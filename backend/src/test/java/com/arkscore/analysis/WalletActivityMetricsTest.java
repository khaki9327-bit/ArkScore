package com.arkscore.analysis;

import static org.assertj.core.api.Assertions.assertThat;

import com.arkscore.walletdata.WalletData;
import java.util.List;
import org.junit.jupiter.api.Test;

class WalletActivityMetricsTest {

    @Test
    void calculatesEightSolanaRpcMetrics() {
        WalletActivityMetrics metrics = WalletActivityMetrics.from(new WalletData(
                "wallet123",
                1_500_000_000L,
                List.of(
                        new WalletData.TokenHolding("token1", "MintA", "100", 6),
                        new WalletData.TokenHolding("token2", "MintA", "250", 6),
                        new WalletData.TokenHolding("token3", "MintB", "0", 6),
                        new WalletData.TokenHolding("token4", "MintC", "500", 6)
                ),
                List.of(
                        new WalletData.RecentTransaction("sig1", 1L, 1_700_000_000L, false),
                        new WalletData.RecentTransaction("sig2", 2L, 1_700_000_010L, true),
                        new WalletData.RecentTransaction("sig3", 3L, 1_700_000_020L, false),
                        new WalletData.RecentTransaction("sig4", 4L, 1_700_000_090L, false)
                ),
                List.of(
                        new WalletData.TransactionDetails("sig1", List.of("wallet123", "account1")),
                        new WalletData.TransactionDetails("sig2", List.of("account1", "account2")),
                        new WalletData.TransactionDetails("sig3", List.of("account3"))
                )
        ));

        assertThat(metrics.solBalance()).isEqualTo(1.5);
        assertThat(metrics.tokenHoldingCount()).isEqualTo(2);
        assertThat(metrics.recentTransactionCount()).isEqualTo(4);
        assertThat(metrics.failedTransactionCount()).isEqualTo(1);
        assertThat(metrics.failedTransactionRatio()).isEqualTo(0.25F);
        assertThat(metrics.recentTransactionTimeSpanSeconds()).isEqualTo(90);
        assertThat(metrics.averageTransactionIntervalSeconds()).isEqualTo(30);
        assertThat(metrics.burstWindowMax60Seconds()).isEqualTo(3);
        assertThat(metrics.involvedAccountCount()).isEqualTo(4);
    }

    @Test
    void handlesEmptyAndPartialTransactionData() {
        WalletActivityMetrics metrics = WalletActivityMetrics.from(new WalletData(
                "wallet123",
                0L,
                List.of(),
                List.of(
                        new WalletData.RecentTransaction("sig1", 1L, null, false)
                ),
                List.of(
                        new WalletData.TransactionDetails("sig1", List.of())
                )
        ));

        assertThat(metrics.solBalance()).isZero();
        assertThat(metrics.tokenHoldingCount()).isZero();
        assertThat(metrics.recentTransactionCount()).isEqualTo(1);
        assertThat(metrics.failedTransactionRatio()).isZero();
        assertThat(metrics.recentTransactionTimeSpanSeconds()).isZero();
        assertThat(metrics.averageTransactionIntervalSeconds()).isZero();
        assertThat(metrics.burstWindowMax60Seconds()).isZero();
        assertThat(metrics.involvedAccountCount()).isZero();
        assertThat(metrics.latestTransactionTimestamp()).isNull();
    }
}
