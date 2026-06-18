package com.codercollie.insurance_lab_core.dto.claim;


import com.codercollie.insurance_lab_core.domain.ClaimStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ClaimResponse(
        Long id,
        String claimNumber,
        Long policyId,
        LocalDate lossDate,
        LocalDate noticeDate,
        BigDecimal claimedAmount,
        ClaimStatus status
) {
}