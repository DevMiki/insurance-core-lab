package com.codercollie.insurance_lab_core.domain;

import java.math.BigDecimal;

public record Coverage(
        String coverageCode,
        String description,
        BigDecimal insuredAmount) {

    public Coverage {
        if (coverageCode == null || coverageCode.isBlank()) {
            throw new IllegalArgumentException("coverageCode is required");
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("description is required");
        }

        if (insuredAmount == null || insuredAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("insuredAmount is required");
        }
    }
}
