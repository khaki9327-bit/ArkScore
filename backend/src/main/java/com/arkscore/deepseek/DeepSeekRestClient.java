package com.arkscore.deepseek;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class DeepSeekRestClient implements DeepSeekClient {

    private static final Logger log = LoggerFactory.getLogger(DeepSeekRestClient.class);
    private static final String FAILURE_MESSAGE = "AI summary generation failed.";

    private final RestClient deepSeekRestClient;
    private final DeepSeekProperties properties;

    public DeepSeekRestClient(
            @Qualifier("deepSeekHttpClient") RestClient deepSeekRestClient,
            DeepSeekProperties properties
    ) {
        this.deepSeekRestClient = deepSeekRestClient;
        this.properties = properties;
    }

    @Override
    public String chat(String systemPrompt, String userPrompt) {
        long startedAt = System.nanoTime();
        log.debug(
                "Calling DeepSeek chat API: model={}, maxTokens={}, temperature={}, thinkingEnabled={}, systemPromptLength={}, userPromptLength={}",
                properties.getModel(),
                properties.getMaxTokens(),
                properties.getTemperature(),
                properties.isThinkingEnabled(),
                systemPrompt.length(),
                userPrompt.length()
        );

        try {
            DeepSeekChatResponse response = deepSeekRestClient.post()
                    .uri("/chat/completions")
                    .body(buildRequest(systemPrompt, userPrompt))
                    .retrieve()
                    .body(DeepSeekChatResponse.class);

            String content = extractContent(response);
            log.debug(
                    "DeepSeek chat API succeeded: durationMs={}, contentLength={}",
                    elapsedMillis(startedAt),
                    content.length()
            );

            return content;
        } catch (DeepSeekClientException exception) {
            log.error(
                    "DeepSeek chat API failed: durationMs={}, message={}",
                    elapsedMillis(startedAt),
                    exception.getMessage(),
                    exception
            );
            throw exception;
        } catch (RestClientException exception) {
            log.error(
                    "DeepSeek chat API request failed: durationMs={}, message={}",
                    elapsedMillis(startedAt),
                    exception.getMessage(),
                    exception
            );
            throw new DeepSeekClientException(FAILURE_MESSAGE, exception);
        }
    }

    private long elapsedMillis(long startedAt) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
    }

    private DeepSeekChatRequest buildRequest(String systemPrompt, String userPrompt) {
        return new DeepSeekChatRequest(
                properties.getModel(),
                List.of(
                        new DeepSeekMessage("system", systemPrompt),
                        new DeepSeekMessage("user", userPrompt)
                ),
                properties.getTemperature(),
                properties.getMaxTokens(),
                new DeepSeekThinking(properties.isThinkingEnabled() ? "enabled" : "disabled")
        );
    }

    private String extractContent(DeepSeekChatResponse response) {
        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new DeepSeekClientException(FAILURE_MESSAGE);
        }

        DeepSeekChoice choice = response.choices().get(0);

        if (choice == null || choice.message() == null || choice.message().content() == null) {
            throw new DeepSeekClientException(FAILURE_MESSAGE);
        }

        String content = choice.message().content().trim();

        if (content.isEmpty()) {
            throw new DeepSeekClientException(FAILURE_MESSAGE);
        }

        return content;
    }

    private record DeepSeekChatRequest(
            String model,
            List<DeepSeekMessage> messages,
            double temperature,
            @JsonProperty("max_tokens") int maxTokens,
            DeepSeekThinking thinking
    ) {
    }

    private record DeepSeekMessage(String role, String content) {
    }

    private record DeepSeekThinking(String type) {
    }

    private record DeepSeekChatResponse(List<DeepSeekChoice> choices) {
    }

    private record DeepSeekChoice(DeepSeekMessage message) {
    }
}
