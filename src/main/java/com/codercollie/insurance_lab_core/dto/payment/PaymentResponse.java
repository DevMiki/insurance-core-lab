package com.codercollie.insurance_lab_core.dto.payment;

import com.codercollie.insurance_lab_core.domain.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentResponse(
        Long id,
        String externalReference,
        Long policyId,
        BigDecimal amount,
        LocalDate paymentDate,
        PaymentStatus status
) {
}
