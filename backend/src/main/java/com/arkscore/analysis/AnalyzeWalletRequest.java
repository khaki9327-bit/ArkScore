package com.arkscore.analysis;

import jakarta.validation.constraints.NotBlank;

public record AnalyzeWalletRequest(
        @NotBlank(message = "Wallet address is required.")
        String walletAddress
) {
}
