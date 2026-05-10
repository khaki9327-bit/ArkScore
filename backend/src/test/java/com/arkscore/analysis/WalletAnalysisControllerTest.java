package com.arkscore.analysis;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.arkscore.walletdata.WalletData;
import com.arkscore.walletdata.WalletDataProvider;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "arkscore.deepseek.api-key=test-key",
        "arkscore.deepseek.model=test-model"
})
@AutoConfigureMockMvc
@Import(WalletAnalysisControllerTest.StubAiSummaryConfig.class)
class WalletAnalysisControllerTest {

    private static final String VALID_WALLET_ADDRESS =
            "9xQeWvG816bUx9EPfVhYScN46nCWgQVPk9JyG9GdM2nb";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void validWalletReturnsAnalysis() throws Exception {
        mockMvc.perform(post("/api/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "walletAddress": "%s"
                                }
                                """.formatted(VALID_WALLET_ADDRESS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.walletAddress").value(VALID_WALLET_ADDRESS))
                .andExpect(jsonPath("$.reputationScore").value(greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.reputationScore").value(lessThanOrEqualTo(100)))
                .andExpect(jsonPath("$.riskLevel").value("Medium"))
                .andExpect(jsonPath("$.tags", hasSize(5)))
                .andExpect(jsonPath("$.risks", hasSize(4)))
                .andExpect(jsonPath("$.summary").value("Stubbed AI summary."))
                .andExpect(jsonPath("$.generatedAt", not(blankOrNullString())));
    }

    @Test
    void blankWalletReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "walletAddress": "   "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Wallet address is required."))
                .andExpect(jsonPath("$.timestamp", not(blankOrNullString())));
    }

    @Test
    void invalidBase58WalletReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "walletAddress": "00000000000000000000000000000000"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Wallet address must be a valid base58 Solana address."))
                .andExpect(jsonPath("$.timestamp", not(blankOrNullString())));
    }

    @Test
    void responseContainsRiskTagsAndSummary() throws Exception {
        mockMvc.perform(post("/api/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "walletAddress": "%s"
                                }
                """.formatted(VALID_WALLET_ADDRESS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags[0]").value("Organic Activity"))
                .andExpect(jsonPath("$.risks[0].label").value("Bot Risk"))
                .andExpect(jsonPath("$.risks[0].level").value("Medium"))
                .andExpect(jsonPath("$.risks[0].value").value(64))
                .andExpect(jsonPath("$.summary", not(blankOrNullString())));
    }

    @TestConfiguration
    static class StubAiSummaryConfig {

        @Bean
        @Primary
        AiSummaryGenerator aiSummaryGenerator() {
            return facts -> "Stubbed AI summary.";
        }

        @Bean
        @Primary
        WalletDataProvider walletDataProvider() {
            return walletAddress -> new WalletData(
                    walletAddress,
                    1_200_000_000L,
                    List.of(
                            new WalletData.TokenHolding("token1", "MintA", "100", 6),
                            new WalletData.TokenHolding("token2", "MintB", "200", 6),
                            new WalletData.TokenHolding("token3", "MintC", "300", 6),
                            new WalletData.TokenHolding("token4", "MintD", "400", 6),
                            new WalletData.TokenHolding("token5", "MintE", "500", 6)
                    ),
                    List.of(
                            new WalletData.RecentTransaction("sig1", 1L, 1_700_000_000L, false),
                            new WalletData.RecentTransaction("sig2", 2L, 1_700_000_010L, false),
                            new WalletData.RecentTransaction("sig3", 3L, 1_700_000_020L, true),
                            new WalletData.RecentTransaction("sig4", 4L, 1_700_000_030L, false),
                            new WalletData.RecentTransaction("sig5", 5L, 1_700_000_040L, false)
                    ),
                    List.of(
                            new WalletData.TransactionDetails(
                                    "sig1",
                                    List.of(walletAddress, "account1", "account2")
                            ),
                            new WalletData.TransactionDetails(
                                    "sig2",
                                    List.of(walletAddress, "account3", "account4")
                            )
                    )
            );
        }
    }
}
