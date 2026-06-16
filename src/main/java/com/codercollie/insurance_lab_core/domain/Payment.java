package com.codercollie.insurance_lab_core.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public record Payment(
        String externalReference,
        String policyNumber,
        BigDecimal amount,
        LocalDate paymentDate,
        PaymentStatus status
) {

    public Payment {
        if (externalReference == null || externalReference.isBlank()) {
            throw new IllegalArgumentException("externalReference is required");
        }

        if (policyNumber == null || policyNumber.isBlank()) {
            throw new IllegalArgumentException("policyNumber is required");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount must be greater than zero");
        }

        Objects.requireNonNull(paymentDate, "paymentDate is required");

        Objects.requireNonNull(status, "status is required");
    }
}
