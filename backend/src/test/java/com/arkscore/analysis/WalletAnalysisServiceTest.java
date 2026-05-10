package com.arkscore.analysis;

import static org.assertj.core.api.Assertions.assertThat;

import com.arkscore.walletdata.WalletData;
import java.util.List;
import org.junit.jupiter.api.Test;

class WalletAnalysisServiceTest {

    @Test
    void solanaRpcDataDrivesDeterministicRuleBasedAnalysis() {
        WalletAnalysisService service = new WalletAnalysisService(
                facts -> "summary",
                walletAddress -> walletData(walletAddress)
        );

        WalletAnalysisResponse response = service.analyze(
                "9xQeWvG816bUx9EPfVhYScN46nCWgQVPk9JyG9GdM2nb"
        );

        assertThat(response.reputationScore()).isBetween(0, 100);
        assertThat(response.riskLevel()).isIn("Low", "Medium", "High");
        assertThat(response.tags()).contains("High Recent Activity", "Diversified Holdings");
        assertThat(response.risks()).hasSize(4);
        assertThat(response.risks())
                .extracting(RiskSignalResponse::label)
                .containsExactly("Bot Risk", "Rug Risk", "Sybil Risk", "Meme Exposure");
        assertThat(response.summary()).isEqualTo("summary");
    }

    @Test
    void summaryFactsIncludeSolanaRpcMetricsAndEvidence() {
        CapturingSummaryGenerator summaryGenerator = new CapturingSummaryGenerator();
        WalletAnalysisService service = new WalletAnalysisService(
                summaryGenerator,
                walletAddress -> walletData(walletAddress)
        );

        service.analyze("9xQeWvG816bUx9EPfVhYScN46nCWgQVPk9JyG9GdM2nb");

        assertThat(summaryGenerator.lastFacts.metrics().solBalance()).isEqualTo(2.5);
        assertThat(summaryGenerator.lastFacts.metrics().tokenHoldingCount()).isEqualTo(5);
        assertThat(summaryGenerator.lastFacts.metrics().recentTransactionCount()).isEqualTo(20);
        assertThat(summaryGenerator.lastFacts.evidence())
                .anySatisfy(evidence -> assertThat(evidence).startsWith("SOL balance:"))
                .anySatisfy(evidence -> assertThat(evidence)
                        .startsWith("Solana public RPC recent transactions analyzed:"))
                .anySatisfy(evidence -> assertThat(evidence)
                        .startsWith("Unique involved accounts across recent transactions:"));
    }

    private WalletData walletData(String walletAddress) {
        return new WalletData(
                walletAddress,
                2_500_000_000L,
                List.of(
                        new WalletData.TokenHolding("token1", "MintA", "100", 6),
                        new WalletData.TokenHolding("token2", "MintB", "200", 6),
                        new WalletData.TokenHolding("token3", "MintC", "300", 6),
                        new WalletData.TokenHolding("token4", "MintD", "400", 6),
                        new WalletData.TokenHolding("token5", "MintE", "500", 6),
                        new WalletData.TokenHolding("token6", "MintE", "0", 6)
                ),
                List.of(
                        tx("sig1", 1_700_000_000L, false),
                        tx("sig2", 1_700_000_100L, false),
                        tx("sig3", 1_700_000_200L, false),
                        tx("sig4", 1_700_000_300L, false),
                        tx("sig5", 1_700_000_400L, false),
                        tx("sig6", 1_700_000_500L, false),
                        tx("sig7", 1_700_000_600L, false),
                        tx("sig8", 1_700_000_700L, false),
                        tx("sig9", 1_700_000_800L, false),
                        tx("sig10", 1_700_000_900L, false),
                        tx("sig11", 1_700_001_000L, false),
                        tx("sig12", 1_700_001_100L, false),
                        tx("sig13", 1_700_001_200L, false),
                        tx("sig14", 1_700_001_300L, false),
                        tx("sig15", 1_700_001_400L, false),
                        tx("sig16", 1_700_001_500L, false),
                        tx("sig17", 1_700_001_600L, false),
                        tx("sig18", 1_700_001_700L, false),
                        tx("sig19", 1_700_001_800L, false),
                        tx("sig20", 1_700_001_900L, false)
                ),
                List.of(
                        details("sig1", walletAddress, "account1", "account2"),
                        details("sig2", walletAddress, "account3", "account4"),
                        details("sig3", walletAddress, "account5", "account6"),
                        details("sig4", walletAddress, "account7", "account8"),
                        details("sig5", walletAddress, "account9", "account10")
                )
        );
    }

    private WalletData.RecentTransaction tx(String signature, long blockTime, boolean failed) {
        return new WalletData.RecentTransaction(signature, 1L, blockTime, failed);
    }

    private WalletData.TransactionDetails details(String signature, String... accountKeys) {
        return new WalletData.TransactionDetails(signature, List.of(accountKeys));
    }

    private static class CapturingSummaryGenerator implements AiSummaryGenerator {

        private WalletAnalysisFacts lastFacts;

        @Override
        public String generateSummary(WalletAnalysisFacts facts) {
            lastFacts = facts;
            return "summary";
        }
    }
}
