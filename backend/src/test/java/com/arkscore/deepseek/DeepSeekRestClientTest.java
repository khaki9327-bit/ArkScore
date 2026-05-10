package com.arkscore.deepseek;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class DeepSeekRestClientTest {

    private MockRestServiceServer server;
    private DeepSeekRestClient client;

    @BeforeEach
    void setUp() {
        DeepSeekProperties properties = new DeepSeekProperties();
        properties.setBaseUrl("https://api.deepseek.test");
        properties.setApiKey("test-key");
        properties.setModel("deepseek-test");

        RestClient.Builder builder = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        server = MockRestServiceServer.bindTo(builder).build();
        client = new DeepSeekRestClient(builder.build(), properties);
    }

    @Test
    void successfulResponseReturnsSummaryAndSendsConfiguredRequest() {
        server.expect(requestTo("https://api.deepseek.test/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-key"))
                .andExpect(jsonPath("$.model").value("deepseek-test"))
                .andExpect(jsonPath("$.max_tokens").value(180))
                .andExpect(jsonPath("$.temperature").value(0.0))
                .andExpect(jsonPath("$.thinking.type").value("disabled"))
                .andExpect(jsonPath("$.messages[0].role").value("system"))
                .andExpect(jsonPath("$.messages[0].content").value("system prompt"))
                .andExpect(jsonPath("$.messages[1].role").value("user"))
                .andExpect(jsonPath("$.messages[1].content").value("user prompt"))
                .andRespond(withSuccess("""
                        {
                          "choices": [
                            {
                              "message": {
                                "content": "Generated wallet summary."
                              }
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        String summary = client.chat("system prompt", "user prompt");

        assertThat(summary).isEqualTo("Generated wallet summary.");
        server.verify();
    }

    @Test
    void nonSuccessResponseThrowsDeepSeekException() {
        server.expect(requestTo("https://api.deepseek.test/chat/completions"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.chat("system prompt", "user prompt"))
                .isInstanceOf(DeepSeekClientException.class)
                .hasMessage("AI summary generation failed.");

        server.verify();
    }

    @Test
    void emptyChoicesThrowDeepSeekException() {
        server.expect(requestTo("https://api.deepseek.test/chat/completions"))
                .andRespond(withSuccess("{\"choices\":[]}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.chat("system prompt", "user prompt"))
                .isInstanceOf(DeepSeekClientException.class)
                .hasMessage("AI summary generation failed.");

        server.verify();
    }

    @Test
    void blankContentThrowsDeepSeekException() {
        server.expect(requestTo("https://api.deepseek.test/chat/completions"))
                .andRespond(withSuccess("""
                        {
                          "choices": [
                            {
                              "message": {
                                "content": "   "
                              }
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.chat("system prompt", "user prompt"))
                .isInstanceOf(DeepSeekClientException.class)
                .hasMessage("AI summary generation failed.");

        server.verify();
    }
}
