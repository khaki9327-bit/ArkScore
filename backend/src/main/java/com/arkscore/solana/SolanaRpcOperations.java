package com.arkscore.solana;

import com.arkscore.walletdata.WalletData;
import java.util.List;

public interface SolanaRpcOperations {

    long getBalanceLamports(String walletAddress);

    List<WalletData.TokenHolding> getTokenAccountsByOwner(String walletAddress);

    List<WalletData.RecentTransaction> getSignaturesForAddress(String walletAddress, int limit);

    WalletData.TransactionDetails getTransactionDetails(String signature);
}
