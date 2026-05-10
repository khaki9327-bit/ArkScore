package com.arkscore.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "arkscore.cors")
public class CorsProperties {

    private List<String> allowedOrigins = new ArrayList<>(List.of(
            "http://localhost:3000",
            "http://127.0.0.1:3000"
    ));

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins == null
                ? new ArrayList<>()
                : new ArrayList<>(allowedOrigins);
    }
}
