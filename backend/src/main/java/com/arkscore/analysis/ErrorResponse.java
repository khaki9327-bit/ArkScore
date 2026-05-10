package com.arkscore.analysis;

import java.time.Instant;

public record ErrorResponse(
        String message,
        Instant timestamp
) {
}
