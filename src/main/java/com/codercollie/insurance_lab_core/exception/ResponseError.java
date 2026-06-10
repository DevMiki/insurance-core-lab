package com.codercollie.insurance_lab_core.exception;

import java.time.Instant;

public record ResponseError(
        Instant timestamp,
        int status,
        String error,
        String message
) {
}
