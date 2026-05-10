package com.arkscore.analysis;

import com.arkscore.deepseek.DeepSeekClient;
import com.arkscore.util.HashUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class WalletAiSummaryService implements AiSummaryGenerator {

    private static final Logger log = LoggerFactory.getLogger(WalletAiSummaryService.class);

    private final DeepSeekClient deepSeekClient;
    private final AiSummaryCache aiSummaryCache;
    private final ObjectMapper objectMapper;

    public WalletAiSummaryService(
            DeepSeekClient deepSeekClient,
            AiSummaryCache aiSummaryCache,
            ObjectMapper objectMapper
    ) {
        this.deepSeekClient = deepSeekClient;
        this.aiSummaryCache = aiSummaryCache;
        this.objectMapper = objectMapper;
    }

    @Override
    public String generateSummary(WalletAnalysisFacts facts) {
        String analysisJson = toJson(facts);
        String cacheKey = facts.walletAddress() + ":" + HashUtils.sha256(analysisJson);

        return aiSummaryCache.get(cacheKey)
                .map(summary -> {
                    log.debug(
                            "AI summary cache hit: walletAddress={}, cacheKeyHash={}",
                            facts.walletAddress(),
                            HashUtils.sha256(cacheKey)
                    );
                    return summary;
                })
                .orElseGet(() -> generateAndCacheSummary(facts, analysisJson, cacheKey));
    }

    private String generateAndCacheSummary(
            WalletAnalysisFacts facts,
            String analysisJson,
            String cacheKey
    ) {
        log.debug("AI summary cache miss: walletAddress={}", facts.walletAddress());

        String summary = deepSeekClient.chat(
                buildSystemPrompt(),
                buildUserPrompt(analysisJson)
        );

        aiSummaryCache.put(cacheKey, summary);

        return summary;
    }

    private String toJson(WalletAnalysisFacts facts) {
        try {
            return objectMapper.writeValueAsString(facts);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Failed to serialize wallet analysis facts",
                    exception
            );
        }
    }

    private String buildSystemPrompt() {
        return """
                You are ArkScore, an AI-powered onchain wallet reputation analyst for Solana.

                Your job is to explain wallet reputation based only on provided backend analysis data.

                Rules:
                - Do not calculate or change the reputation score.
                - Do not create new tags.
                - Do not invent wallet behaviors.
                - Do not provide financial advice.
                - Do not claim certainty.
                - Use concise and professional language.
                - Focus on wallet behavior, risk patterns, and trust signals.
                - Return only the requested sections.
                - Do not include hidden reasoning or chain-of-thought.
                """;
    }

    private String buildUserPrompt(String walletAnalysisJson) {
        return """
                Analyze the following Solana wallet behavior data and generate a concise wallet reputation summary.

                Return exactly 4 sections:

                1. Overall Assessment
                2. Key Trust Signals
                3. Key Risk Signals
                4. Final Summary

                Rules:
                - Keep each section within 2 sentences.
                - Do not change the score.
                - Do not create new tags.
                - Use the given wallet analysis data only.
                - Do not provide financial advice.

                Wallet Data:
                %s
                """.formatted(walletAnalysisJson);
    }
}
