package com.arkscore.deepseek;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

class DeepSeekPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(PropertiesConfig.class);

    @Test
    void defaultsAreAppliedWhenOptionalValuesAreNotConfigured() {
        contextRunner
                .withPropertyValues(
                        "arkscore.deepseek.api-key=test-key",
                        "arkscore.deepseek.model=test-model"
                )
                .run(context -> {
                    assertThat(context).hasNotFailed();

                    DeepSeekProperties properties = context.getBean(DeepSeekProperties.class);

                    assertThat(properties.getBaseUrl()).isEqualTo("https://api.deepseek.com");
                    assertThat(properties.getMaxTokens()).isEqualTo(180);
                    assertThat(properties.getTemperature()).isEqualTo(0.0);
                    assertThat(properties.isThinkingEnabled()).isFalse();
                });
    }

    @Test
    void missingApiKeyFailsContextStartup() {
        contextRunner
                .withPropertyValues("arkscore.deepseek.model=test-model")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void missingModelFailsContextStartup() {
        contextRunner
                .withPropertyValues("arkscore.deepseek.api-key=test-key")
                .run(context -> assertThat(context).hasFailed());
    }

    @Configuration
    @EnableConfigurationProperties(DeepSeekProperties.class)
    static class PropertiesConfig {
    }
}
