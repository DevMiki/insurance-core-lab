package com.codercollie.insurance_lab_core.dto.claim;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateClaimRequest(
        @NotNull(message = "policyId is required")
        Long policyId,

        @NotNull(message = "lossDate is required")
        LocalDate lossDate,

        @NotNull(message = "noticeDate is required")
        LocalDate noticeDate,

        @NotNull(message = "claimedAmount is required")
        @DecimalMin(
                value = "0.01",
                message = "claimedAmount must be greater than zero"
        )
        BigDecimal claimedAmount
) {
}
