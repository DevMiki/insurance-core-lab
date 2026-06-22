package com.codercollie.insurance_lab_core.dto.claim;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ReserveClaimRequest(
        @NotNull(message = "amount is required")
        @DecimalMin(
                value = "0.01",
                message = "amount must be greater than zero"
        )
        BigDecimal amount
) {
}
