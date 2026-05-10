package com.arkscore.analysis;

import static org.assertj.core.api.Assertions.assertThat;

import com.arkscore.deepseek.DeepSeekClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WalletAiSummaryServiceTest {

    private RecordingDeepSeekClient deepSeekClient;
    private WalletAiSummaryService service;

    @BeforeEach
    void setUp() {
        deepSeekClient = new RecordingDeepSeekClient();
        service = new WalletAiSummaryService(
                deepSeekClient,
                new AiSummaryCache(),
                new ObjectMapper()
        );
    }

    @Test
    void sameWalletAndAnalysisJsonReturnsCachedSummary() {
        WalletAnalysisFacts facts = facts(
                "9xQeWvG816bUx9EPfVhYScN46nCWgQVPk9JyG9GdM2nb",
                82
        );

        String firstSummary = service.generateSummary(facts);
        String secondSummary = service.generateSummary(facts);

        assertThat(firstSummary).isEqualTo("summary-1");
        assertThat(secondSummary).isEqualTo("summary-1");
        assertThat(deepSeekClient.callCount()).isEqualTo(1);
    }

    @Test
    void differentWalletUsesDifferentCacheKey() {
        service.generateSummary(facts(
                "9xQeWvG816bUx9EPfVhYScN46nCWgQVPk9JyG9GdM2nb",
                82
        ));
        service.generateSummary(facts(
                "7xQeWvG816bUx9EPfVhYScN46nCWgQVPk9JyG9GdM2na",
                82
        ));

        assertThat(deepSeekClient.callCount()).isEqualTo(2);
    }

    @Test
    void changedAnalysisDataUsesDifferentCacheKey() {
        String walletAddress = "9xQeWvG816bUx9EPfVhYScN46nCWgQVPk9JyG9GdM2nb";

        service.generateSummary(facts(walletAddress, 82));
        service.generateSummary(facts(walletAddress, 83));

        assertThat(deepSeekClient.callCount()).isEqualTo(2);
    }

    @Test
    void promptContainsAnalysisJsonAndGuardrails() {
        WalletAnalysisFacts facts = facts(
                "9xQeWvG816bUx9EPfVhYScN46nCWgQVPk9JyG9GdM2nb",
                82
        );

        service.generateSummary(facts);

        assertThat(deepSeekClient.lastSystemPrompt())
                .contains("Do not calculate or change the reputation score.")
                .contains("Do not create new tags.")
                .contains("Do not provide financial advice.")
                .contains("Do not claim certainty.");
        assertThat(deepSeekClient.lastUserPrompt())
                .contains("Return exactly 4 sections")
                .contains("1. Overall Assessment")
                .contains("2. Key Trust Signals")
                .contains("3. Key Risk Signals")
                .contains("4. Final Summary")
                .contains("\"walletAddress\":\"9xQeWvG816bUx9EPfVhYScN46nCWgQVPk9JyG9GdM2nb\"")
                .contains("\"reputationScore\":82")
                .contains("\"tags\":[\"Long-term Holder\",\"Organic Activity\"]");
    }

    private WalletAnalysisFacts facts(String walletAddress, int reputationScore) {
        return new WalletAnalysisFacts(
                walletAddress,
                reputationScore,
                "Low to Medium",
                List.of("Long-term Holder", "Organic Activity"),
                List.of(new RiskSignalResponse(
                        "Bot Risk",
                        "Low",
                        18,
                        "Transaction cadence looks human."
                )),
                new WalletActivityMetrics(
                        1.2,
                        4,
                        12,
                        1,
                        0.08F,
                        600,
                        54,
                        2,
                        18,
                        "2026-05-09T00:00:00Z"
                ),
                List.of("Solana public RPC recent transactions analyzed: 12")
        );
    }

    private static class RecordingDeepSeekClient implements DeepSeekClient {

        private final List<String> systemPrompts = new ArrayList<>();
        private final List<String> userPrompts = new ArrayList<>();

        @Override
        public String chat(String systemPrompt, String userPrompt) {
            systemPrompts.add(systemPrompt);
            userPrompts.add(userPrompt);

            return "summary-" + systemPrompts.size();
        }

        int callCount() {
            return systemPrompts.size();
        }

        String lastSystemPrompt() {
            return systemPrompts.get(systemPrompts.size() - 1);
        }

        String lastUserPrompt() {
            return userPrompts.get(userPrompts.size() - 1);
        }
    }
}
