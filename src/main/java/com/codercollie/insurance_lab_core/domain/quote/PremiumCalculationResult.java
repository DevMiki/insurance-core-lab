package com.codercollie.insurance_lab_core.domain.quote;

import java.math.BigDecimal;

public record PremiumCalculationResult(
        BigDecimal netPremium,
        BigDecimal taxAmount,
        BigDecimal totalAmount
) {
}
