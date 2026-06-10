package com.codercollie.insurance_lab_core.dto.coverage;

import java.math.BigDecimal;

public record CoverageResponse(
        Long id,
        String code,
        String name,
        String description,
        BigDecimal basePrice,
        Long productId
) {
}
