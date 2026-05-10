package com.arkscore.analysis;

import java.time.Instant;
import java.util.List;

public record WalletAnalysisResponse(
        String walletAddress,
        int reputationScore,
        String riskLevel,
        List<String> tags,
        List<RiskSignalResponse> risks,
        String summary,
        Instant generatedAt
) {
}
