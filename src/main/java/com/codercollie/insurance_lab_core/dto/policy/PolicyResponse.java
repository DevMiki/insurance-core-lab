package com.codercollie.insurance_lab_core.dto.policy;

import com.codercollie.insurance_lab_core.domain.PolicyStatus;

import java.time.LocalDate;
import java.util.List;

public record PolicyResponse(
        Long id,
        String policyNumber,
        Long quoteId,
        Long customerId,
        Long productId,
        List<Long> coverageIds,
        LocalDate startDate,
        LocalDate endDate,
        PolicyStatus status
) {
}
