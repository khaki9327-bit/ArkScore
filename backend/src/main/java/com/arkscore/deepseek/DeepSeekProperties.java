package com.arkscore.deepseek;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "arkscore.deepseek")
public class DeepSeekProperties {

    @NotBlank(message = "DeepSeek base URL is required.")
    private String baseUrl = "https://api.deepseek.com";

    @NotBlank(message = "DeepSeek API key is required.")
    private String apiKey;

    @NotBlank(message = "DeepSeek model is required.")
    private String model;

    @Min(value = 1, message = "DeepSeek max tokens must be greater than zero.")
    private int maxTokens = 180;

    @DecimalMin(value = "0.0", message = "DeepSeek temperature must not be negative.")
    @DecimalMax(value = "2.0", message = "DeepSeek temperature must not exceed 2.")
    private double temperature = 0.0;

    private boolean thinkingEnabled = false;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public boolean isThinkingEnabled() {
        return thinkingEnabled;
    }

    public void setThinkingEnabled(boolean thinkingEnabled) {
        this.thinkingEnabled = thinkingEnabled;
    }
}
