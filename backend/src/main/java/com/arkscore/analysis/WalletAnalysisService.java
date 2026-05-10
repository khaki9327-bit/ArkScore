package com.arkscore.analysis;

import com.arkscore.walletdata.WalletData;
import com.arkscore.walletdata.WalletDataProvider;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class WalletAnalysisService {

    private static final Pattern BASE58_SOLANA_ADDRESS =
            Pattern.compile("^[1-9A-HJ-NP-Za-km-z]+$");

    private final AiSummaryGenerator aiSummaryGenerator;
    private final WalletDataProvider walletDataProvider;

    public WalletAnalysisService(
            AiSummaryGenerator aiSummaryGenerator,
            WalletDataProvider walletDataProvider
    ) {
        this.aiSummaryGenerator = aiSummaryGenerator;
        this.walletDataProvider = walletDataProvider;
    }

    public WalletAnalysisResponse analyze(String walletAddress) {
        String normalizedAddress = walletAddress.trim();
        validateWalletAddress(normalizedAddress);

        WalletData walletData = walletDataProvider.getWalletData(normalizedAddress);
        WalletActivityMetrics metrics = WalletActivityMetrics.from(walletData);

        int botRisk = calculateBotRisk(metrics);
        int rugRisk = calculateRugRisk(metrics);
        int sybilRisk = calculateSybilRisk(metrics);
        int memeExposure = calculateMemeExposure(metrics);
        int averageRisk = Math.round((botRisk + rugRisk + sybilRisk + memeExposure) / 4.0F);
        int reputationScore = clamp(100 - averageRisk + activityBonus(metrics) + diversityBonus(metrics));
        String riskLevel = toOverallRiskLevel(averageRisk);
        List<String> tags = buildTags(metrics, rugRisk, memeExposure);
        List<RiskSignalResponse> risks = List.of(
                new RiskSignalResponse(
                        "Bot Risk",
                        toRiskLevel(botRisk),
                        botRisk,
                        "Estimated from recent transaction count, failure ratio, average interval, and burst behavior."
                ),
                new RiskSignalResponse(
                        "Rug Risk",
                        toRiskLevel(rugRisk),
                        rugRisk,
                        "Estimated conservatively from SOL balance, token holding breadth, and limited public RPC coverage."
                ),
                new RiskSignalResponse(
                        "Sybil Risk",
                        toRiskLevel(sybilRisk),
                        sybilRisk,
                        "Estimated from sparse balances, limited token diversity, activity depth, and involved account breadth."
                ),
                new RiskSignalResponse(
                        "Meme Exposure",
                        toRiskLevel(memeExposure),
                        memeExposure,
                        "Estimated conservatively from token holding breadth because public RPC does not provide token metadata."
                )
        );
        List<String> evidence = buildEvidence(metrics);
        String summary = aiSummaryGenerator.generateSummary(new WalletAnalysisFacts(
                normalizedAddress,
                reputationScore,
                riskLevel,
                tags,
                risks,
                metrics,
                evidence
        ));

        return new WalletAnalysisResponse(
                normalizedAddress,
                reputationScore,
                riskLevel,
                tags,
                risks,
                summary,
                Instant.now()
        );
    }

    private void validateWalletAddress(String walletAddress) {
        if (walletAddress.length() < 32 || walletAddress.length() > 44) {
            throw new InvalidWalletAddressException(
                    "Wallet address must be 32 to 44 characters."
            );
        }

        if (!BASE58_SOLANA_ADDRESS.matcher(walletAddress).matches()) {
            throw new InvalidWalletAddressException(
                    "Wallet address must be a valid base58 Solana address."
            );
        }
    }

    private int calculateBotRisk(WalletActivityMetrics metrics) {
        int value = 10;

        value += Math.round(metrics.failedTransactionRatio() * 45);

        if (metrics.recentTransactionCount() == 0) {
            value += 25;
        } else if (metrics.recentTransactionCount() < 3) {
            value += 15;
        }

        if (metrics.burstWindowMax60Seconds() >= 4) {
            value += 30;
        } else if (metrics.burstWindowMax60Seconds() >= 3) {
            value += 15;
        }

        if (metrics.recentTransactionCount() >= 5
                && metrics.averageTransactionIntervalSeconds() > 0
                && metrics.averageTransactionIntervalSeconds() < 30) {
            value += 15;
        }

        if (metrics.recentTransactionCount() >= 5 && metrics.involvedAccountCount() <= 3) {
            value += 10;
        }

        return clamp(value);
    }

    private int calculateRugRisk(WalletActivityMetrics metrics) {
        int value = 15;

        if (metrics.tokenHoldingCount() >= 20) {
            value += 30;
        } else if (metrics.tokenHoldingCount() >= 10) {
            value += 20;
        } else if (metrics.tokenHoldingCount() >= 5) {
            value += 10;
        }

        if (metrics.solBalance() < 0.05 && metrics.tokenHoldingCount() >= 5) {
            value += 20;
        }

        if (metrics.involvedAccountCount() <= 3 && metrics.tokenHoldingCount() >= 5) {
            value += 10;
        }

        if (metrics.tokenHoldingCount() == 0) {
            value -= 10;
        }

        return clamp(value);
    }

    private int calculateSybilRisk(WalletActivityMetrics metrics) {
        int value = 20;

        if (metrics.solBalance() < 0.05) {
            value += 20;
        } else if (metrics.solBalance() < 0.5) {
            value += 10;
        }

        if (metrics.tokenHoldingCount() <= 1) {
            value += 15;
        }

        if (metrics.recentTransactionCount() < 3) {
            value += 20;
        }

        if (metrics.recentTransactionCount() >= 5 && metrics.recentTransactionTimeSpanSeconds() <= 60) {
            value += 15;
        }

        if (metrics.recentTransactionCount() >= 5 && metrics.involvedAccountCount() <= 5) {
            value += 10;
        }

        if (metrics.tokenHoldingCount() >= 5 && metrics.recentTransactionCount() >= 5) {
            value -= 10;
        }

        return clamp(value);
    }

    private int calculateMemeExposure(WalletActivityMetrics metrics) {
        int value = Math.min(45, metrics.tokenHoldingCount() * 4);

        if (metrics.tokenHoldingCount() >= 10) {
            value += 10;
        }

        if (metrics.tokenHoldingCount() >= 20) {
            value += 10;
        }

        return clamp(value);
    }

    private int activityBonus(WalletActivityMetrics metrics) {
        if (metrics.recentTransactionCount() >= 15
                && metrics.failedTransactionRatio() < 0.2
                && metrics.burstWindowMax60Seconds() < 4) {
            return 8;
        }

        if (metrics.recentTransactionCount() >= 5 && metrics.failedTransactionRatio() < 0.35) {
            return 5;
        }

        return 0;
    }

    private int diversityBonus(WalletActivityMetrics metrics) {
        if (metrics.tokenHoldingCount() >= 10 && metrics.involvedAccountCount() >= 20) {
            return 7;
        }

        if (metrics.tokenHoldingCount() >= 5) {
            return 4;
        }

        return 0;
    }

    private List<String> buildTags(
            WalletActivityMetrics metrics,
            int rugRisk,
            int memeExposure
    ) {
        List<String> tags = new ArrayList<>();

        if (metrics.recentTransactionCount() >= 15) {
            tags.add("High Recent Activity");
        } else if (metrics.recentTransactionCount() >= 5) {
            tags.add("Organic Activity");
        } else {
            tags.add("Limited Activity");
        }

        if (metrics.latestTransactionTimestamp() != null) {
            tags.add("Recently Active");
        }

        if (metrics.tokenHoldingCount() >= 5) {
            tags.add("Diversified Holdings");
        }

        if (metrics.involvedAccountCount() >= 20) {
            tags.add("Broad Account Interaction");
        }

        if (metrics.burstWindowMax60Seconds() >= 4) {
            tags.add("Burst Activity");
        }

        if (rugRisk < 35) {
            tags.add("Low Rug Risk");
        }

        if (memeExposure >= 35) {
            tags.add("Moderate Meme Exposure");
        }

        return tags.stream().limit(5).toList();
    }

    private List<String> buildEvidence(WalletActivityMetrics metrics) {
        List<String> evidence = new ArrayList<>();
        evidence.add("SOL balance: " + metrics.formattedSolBalance());
        evidence.add("Unique non-zero token mints: " + metrics.tokenHoldingCount());
        evidence.add("Solana public RPC recent transactions analyzed: " + metrics.recentTransactionCount());
        evidence.add("Failed transaction ratio: " + String.format(Locale.ROOT, "%.2f", metrics.failedTransactionRatio()));
        evidence.add("Recent transaction time span seconds: " + metrics.recentTransactionTimeSpanSeconds());
        evidence.add("Average transaction interval seconds: " + metrics.averageTransactionIntervalSeconds());
        evidence.add("Max transactions in 60-second burst window: " + metrics.burstWindowMax60Seconds());
        evidence.add("Unique involved accounts across recent transactions: " + metrics.involvedAccountCount());

        if (metrics.latestTransactionTimestamp() != null) {
            evidence.add("Latest transaction timestamp: " + metrics.latestTransactionTimestamp());
        }

        return evidence;
    }

    private String toRiskLevel(int value) {
        if (value < 35) {
            return "Low";
        }

        if (value < 65) {
            return "Medium";
        }

        return "High";
    }

    private String toOverallRiskLevel(int value) {
        return toRiskLevel(value);
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }
}
