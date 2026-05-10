package com.arkscore.solana;

import java.time.Clock;
import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(SolanaRpcProperties.class)
public class SolanaConfig {

    @Bean
    RestClient solanaRpcHttpClient(SolanaRpcProperties properties, RestClient.Builder builder) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(10));
        requestFactory.setReadTimeout(Duration.ofSeconds(30));

        return builder
                .baseUrl(properties.getRpcUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .requestFactory(requestFactory)
                .build();
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
