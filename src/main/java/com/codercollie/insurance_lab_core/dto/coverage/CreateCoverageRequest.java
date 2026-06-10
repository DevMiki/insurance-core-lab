package com.codercollie.insurance_lab_core.dto.coverage;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateCoverageRequest(
        @NotBlank(message = "code is required")
        String code,

        @NotBlank(message = "name is required")
        String name,

        @NotBlank(message = "description is required")
        String description,

        @NotNull(message = "basePrice is required")
        @DecimalMin(value = "0.00", inclusive = false, message = "basePrice must be greater than zero")
        BigDecimal basePrice
) {
}
