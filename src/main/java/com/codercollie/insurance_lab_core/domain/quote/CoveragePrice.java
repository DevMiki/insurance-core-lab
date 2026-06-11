package com.codercollie.insurance_lab_core.domain.quote;

import java.math.BigDecimal;

public record CoveragePrice(
        String coverageCode,
        BigDecimal basePrice
) {

    public CoveragePrice {
        if(coverageCode == null || coverageCode.isBlank()) {
            throw new IllegalArgumentException("coverageCode is required");
        }

        if(basePrice == null || basePrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("basePrice must be greater than zero");
        }
    }

}
