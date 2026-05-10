package com.arkscore.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "arkscore.cors.allowed-origins=https://example.com",
        "arkscore.deepseek.api-key=test-key",
        "arkscore.deepseek.model=test-model"
})
@AutoConfigureMockMvc
class CorsConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void configuredOriginIsAllowedForApiPreflight() throws Exception {
        mockMvc.perform(options("/api/analyze")
                        .header(HttpHeaders.ORIGIN, "https://example.com")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                        "https://example.com"
                ));
    }

    @Test
    void unconfiguredOriginIsRejectedForApiPreflight() throws Exception {
        mockMvc.perform(options("/api/analyze")
                        .header(HttpHeaders.ORIGIN, "https://untrusted.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }
}
