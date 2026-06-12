package com.codercollie.insurance_lab_core.dto.quote;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record QuoteResponse(
        Long id,
        Long productId,
        List<Long> coverageIds,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal netPremium,
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        Instant createdAt
) {
}
