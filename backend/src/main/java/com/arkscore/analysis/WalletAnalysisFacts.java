package com.arkscore.analysis;

import java.util.List;

public record WalletAnalysisFacts(
        String walletAddress,
        int reputationScore,
        String riskLevel,
        List<String> tags,
        List<RiskSignalResponse> risks,
        WalletActivityMetrics metrics,
        List<String> evidence
) {
}
