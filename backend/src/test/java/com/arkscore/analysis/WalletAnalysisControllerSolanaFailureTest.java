package com.arkscore.analysis;

import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.arkscore.solana.SolanaRpcClientException;
import com.arkscore.walletdata.WalletDataProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
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
@Import(WalletAnalysisControllerSolanaFailureTest.FailingSolanaConfig.class)
class WalletAnalysisControllerSolanaFailureTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void solanaRpcFailureReturnsBadGateway() throws Exception {
        mockMvc.perform(post("/api/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "walletAddress": "9xQeWvG816bUx9EPfVhYScN46nCWgQVPk9JyG9GdM2nb"
                                }
                                """))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.message").value("Wallet data provider failed."))
                .andExpect(jsonPath("$.timestamp", not(blankOrNullString())));
    }

    @TestConfiguration
    static class FailingSolanaConfig {

        @Bean
        @Primary
        WalletDataProvider walletDataProvider() {
            return walletAddress -> {
                throw new SolanaRpcClientException("Wallet data provider failed.");
            };
        }
    }
}
