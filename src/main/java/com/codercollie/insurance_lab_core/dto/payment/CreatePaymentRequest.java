package com.codercollie.insurance_lab_core.dto.payment;

import com.codercollie.insurance_lab_core.domain.PaymentStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreatePaymentRequest(

        @NotBlank(message = "externalReference is required")
        String externalReference,

        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", message = "amount must be greater than zero")
        BigDecimal amount,

        @NotNull(message = "paymentDate is required")
        LocalDate paymentDate,

        @NotNull(message = "status is required")
        PaymentStatus status
) {
}
