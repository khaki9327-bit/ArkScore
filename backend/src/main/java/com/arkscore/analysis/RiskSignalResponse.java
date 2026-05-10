package com.arkscore.analysis;

public record RiskSignalResponse(
        String label,
        String level,
        int value,
        String description
) {
}
