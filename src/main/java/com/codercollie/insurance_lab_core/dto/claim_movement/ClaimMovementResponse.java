package com.codercollie.insurance_lab_core.dto.claim_movement;

import com.codercollie.insurance_lab_core.domain.ClaimStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record ClaimMovementResponse(
        Long id,
        Long claimId,
        ClaimStatus status,
        BigDecimal amount,
        String note,
        Instant createdAt
) {
}
