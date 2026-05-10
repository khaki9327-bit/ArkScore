package com.arkscore.analysis;

import com.arkscore.walletdata.WalletData;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public record WalletActivityMetrics(
        double solBalance,
        int tokenHoldingCount,
        int recentTransactionCount,
        int failedTransactionCount,
        float failedTransactionRatio,
        long recentTransactionTimeSpanSeconds,
        long averageTransactionIntervalSeconds,
        int burstWindowMax60Seconds,
        int involvedAccountCount,
        String latestTransactionTimestamp
) {

    private static final long LAMPORTS_PER_SOL = 1_000_000_000L;

    public static WalletActivityMetrics from(WalletData walletData) {
        List<WalletData.RecentTransaction> recentTransactions = walletData.recentTransactions();
        List<Long> blockTimes = recentTransactions.stream()
                .map(WalletData.RecentTransaction::blockTime)
                .filter(blockTime -> blockTime != null && blockTime > 0)
                .sorted()
                .toList();
        int recentTransactionCount = recentTransactions.size();
        int failedTransactionCount = (int) recentTransactions.stream()
                .filter(WalletData.RecentTransaction::failed)
                .count();
        long timeSpanSeconds = calculateTimeSpanSeconds(blockTimes);

        return new WalletActivityMetrics(
                walletData.lamports() / (double) LAMPORTS_PER_SOL,
                uniqueNonZeroMintCount(walletData),
                recentTransactionCount,
                failedTransactionCount,
                ratio(failedTransactionCount, recentTransactionCount),
                timeSpanSeconds,
                calculateAverageIntervalSeconds(timeSpanSeconds, recentTransactionCount),
                calculateBurstWindowMax(blockTimes),
                involvedAccountCount(walletData),
                latestTimestamp(blockTimes)
        );
    }

    public String formattedSolBalance() {
        return String.format(Locale.ROOT, "%.4f", solBalance);
    }

    private static int uniqueNonZeroMintCount(WalletData walletData) {
        return (int) walletData.tokenHoldings().stream()
                .filter(WalletData.TokenHolding::hasPositiveAmount)
                .map(WalletData.TokenHolding::mint)
                .filter(mint -> mint != null && !mint.isBlank())
                .distinct()
                .count();
    }

    private static int involvedAccountCount(WalletData walletData) {
        return (int) walletData.transactionDetails().stream()
                .flatMap(details -> details.accountKeys().stream())
                .filter(account -> account != null && !account.isBlank())
                .distinct()
                .count();
    }

    private static long calculateTimeSpanSeconds(List<Long> blockTimes) {
        if (blockTimes.size() < 2) {
            return 0;
        }

        return blockTimes.get(blockTimes.size() - 1) - blockTimes.get(0);
    }

    private static long calculateAverageIntervalSeconds(
            long timeSpanSeconds,
            int recentTransactionCount
    ) {
        if (recentTransactionCount < 2) {
            return 0;
        }

        return timeSpanSeconds / (recentTransactionCount - 1L);
    }

    private static int calculateBurstWindowMax(List<Long> blockTimes) {
        if (blockTimes.isEmpty()) {
            return 0;
        }

        int max = 1;
        int start = 0;
        for (int end = 0; end < blockTimes.size(); end++) {
            while (blockTimes.get(end) - blockTimes.get(start) > 60) {
                start++;
            }

            max = Math.max(max, end - start + 1);
        }

        return max;
    }

    private static String latestTimestamp(List<Long> blockTimes) {
        return blockTimes.stream()
                .max(Comparator.naturalOrder())
                .map(timestamp -> Instant.ofEpochSecond(timestamp)
                        .atOffset(ZoneOffset.UTC)
                        .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .orElse(null);
    }

    private static float ratio(int numerator, int denominator) {
        return denominator <= 0 ? 0 : numerator / (float) denominator;
    }
}
