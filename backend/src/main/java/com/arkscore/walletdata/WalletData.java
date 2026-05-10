package com.arkscore.walletdata;

import java.math.BigInteger;
import java.util.List;

public record WalletData(
        String walletAddress,
        long lamports,
        List<TokenHolding> tokenHoldings,
        List<RecentTransaction> recentTransactions,
        List<TransactionDetails> transactionDetails
) {

    public WalletData {
        tokenHoldings = tokenHoldings == null ? List.of() : List.copyOf(tokenHoldings);
        recentTransactions = recentTransactions == null ? List.of() : List.copyOf(recentTransactions);
        transactionDetails = transactionDetails == null ? List.of() : List.copyOf(transactionDetails);
    }

    public record TokenHolding(
            String tokenAccount,
            String mint,
            String amount,
            int decimals
    ) {

        public boolean hasPositiveAmount() {
            if (amount == null || amount.isBlank()) {
                return false;
            }

            try {
                return new BigInteger(amount).signum() > 0;
            } catch (NumberFormatException exception) {
                return false;
            }
        }
    }

    public record RecentTransaction(
            String signature,
            long slot,
            Long blockTime,
            boolean failed
    ) {
    }

    public record TransactionDetails(
            String signature,
            List<String> accountKeys
    ) {

        public TransactionDetails {
            accountKeys = accountKeys == null ? List.of() : List.copyOf(accountKeys);
        }
    }
}
