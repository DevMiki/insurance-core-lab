package com.codercollie.insurance_lab_core.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public record Claim(
        String claimNumber,
        Policy policy,
        LocalDate lossDate,
        BigDecimal claimedAmount,
        ClaimStatus status
) {

    public Claim {
        if (claimNumber == null || claimNumber.isBlank()) {
            throw new IllegalArgumentException("claimNumber is required");
        }

        Objects.requireNonNull(policy, "policy is required");
        Objects.requireNonNull(lossDate, "lossDate is required");
        Objects.requireNonNull(status, "status is required");

        if (claimedAmount == null || claimedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("claimedAmount must be greater than zero");
        }

        if (policy.status() != PolicyStatus.ACTIVE) {
            throw new IllegalArgumentException("claim can be opened only on an active policy");
        }

        if (lossDate.isBefore(policy.startDate()) || lossDate.isAfter(policy.endDate())) {
            throw new IllegalArgumentException("lossDate must be inside the policy period");
        }
    }
}
