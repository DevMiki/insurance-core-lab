package com.codercollie.insurance_lab_core.service;

import java.math.BigDecimal;

public record PremiumCalculationResult(
        BigDecimal netPremium,
        BigDecimal taxAmount,
        BigDecimal totalAmount
) {
}
