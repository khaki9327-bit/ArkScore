package com.arkscore.solana;

import com.arkscore.walletdata.WalletData;
import com.arkscore.walletdata.WalletDataCache;
import com.arkscore.walletdata.WalletDataProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SolanaWalletDataProvider implements WalletDataProvider {

    private static final Logger log = LoggerFactory.getLogger(SolanaWalletDataProvider.class);

    private final SolanaRpcOperations solanaRpcClient;
    private final SolanaRpcProperties properties;
    private final WalletDataCache walletDataCache;

    public SolanaWalletDataProvider(
            SolanaRpcOperations solanaRpcClient,
            SolanaRpcProperties properties,
            WalletDataCache walletDataCache
    ) {
        this.solanaRpcClient = solanaRpcClient;
        this.properties = properties;
        this.walletDataCache = walletDataCache;
    }

    @Override
    public WalletData getWalletData(String walletAddress) {
        return walletDataCache.get(walletAddress)
                .map(walletData -> {
                    log.debug("Solana wallet data cache hit: walletAddress={}", walletAddress);
                    return walletData;
                })
                .orElseGet(() -> fetchAndCacheWalletData(walletAddress));
    }

    private WalletData fetchAndCacheWalletData(String walletAddress) {
        long startedAt = System.nanoTime();
        log.debug(
                "Fetching Solana wallet data: walletAddress={}, transactionLimit={}, transactionDetailLimit={}",
                walletAddress,
                properties.getTransactionLimit(),
                properties.getTransactionDetailLimit()
        );

        long lamports = solanaRpcClient.getBalanceLamports(walletAddress);
        List<WalletData.TokenHolding> tokenHoldings =
                solanaRpcClient.getTokenAccountsByOwner(walletAddress);
        List<WalletData.RecentTransaction> recentTransactions =
                solanaRpcClient.getSignaturesForAddress(
                        walletAddress,
                        properties.getTransactionLimit()
                );
        List<WalletData.TransactionDetails> transactionDetails = new ArrayList<>();
        int transactionDetailLimit = Math.min(
                recentTransactions.size(),
                properties.getTransactionDetailLimit()
        );
        for (int index = 0; index < transactionDetailLimit; index++) {
            WalletData.RecentTransaction transaction = recentTransactions.get(index);
            try {
                transactionDetails.add(solanaRpcClient.getTransactionDetails(transaction.signature()));
            } catch (SolanaRpcRateLimitException exception) {
                if (!properties.isAllowPartialTransactionDetails()) {
                    throw exception;
                }

                log.warn(
                        "Solana RPC rate limited while fetching transaction details; using partial details: walletAddress={}, fetchedDetails={}, requestedDetails={}",
                        walletAddress,
                        transactionDetails.size(),
                        transactionDetailLimit
                );
                break;
            }
        }

        WalletData walletData = new WalletData(
                walletAddress,
                lamports,
                tokenHoldings,
                recentTransactions,
                transactionDetails
        );
        walletDataCache.put(walletAddress, walletData);

        log.debug(
                "Fetched Solana wallet data: walletAddress={}, durationMs={}, lamports={}, tokenAccounts={}, recentTransactions={}, transactionDetails={}",
                walletAddress,
                elapsedMillis(startedAt),
                lamports,
                tokenHoldings.size(),
                recentTransactions.size(),
                transactionDetails.size()
        );

        return walletData;
    }

    private long elapsedMillis(long startedAt) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
    }
}
