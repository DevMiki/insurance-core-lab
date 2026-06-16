package com.codercollie.insurance_lab_core.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentTest {

    @Test
    void rejectsMissingExternalReference() {
        assertThrows(IllegalArgumentException.class,
                () -> new Payment(
                        null,
                        "POL-2026-0001",
                        new BigDecimal("30.00"),
                        LocalDate.of(2026, 1, 1),
                        PaymentStatus.PENDING
                ));
    }

    @Test
    void rejectsMissingPolicyNumber() {
        assertThrows(IllegalArgumentException.class,
                () -> new Payment(
                        "PAY-2026-0001",
                        null,
                        new BigDecimal("30.00"),
                        LocalDate.of(2026, 1, 1),
                        PaymentStatus.PENDING
                ));
    }

    @Test
    void rejectsMissingAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> new Payment(
                        "PAY-2026-0001",
                        "POL-2026-0001",
                        null,
                        LocalDate.of(2026, 1, 1),
                        PaymentStatus.PENDING
                ));
    }

    @Test
    void rejectsMissingPaymentDate() {
        assertThrows(NullPointerException.class,
                () -> new Payment(
                        "PAY-2026-0001",
                        "POL-2026-0001",
                        new BigDecimal("30.00"),
                        null,
                        PaymentStatus.PENDING
                ));
    }

    @Test
    void rejectsMissingStatus() {
        assertThrows(NullPointerException.class,
                () -> new Payment(
                        "PAY-2026-0001",
                        "POL-2026-0001",
                        new BigDecimal("30.00"),
                        LocalDate.of(2026, 1, 1),
                        null
                ));
    }

    @Test
    void rejectsZeroAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> new Payment(
                        "PAY-2026-0001",
                        "POL-2026-0001",
                        BigDecimal.ZERO,
                        LocalDate.of(2026, 1, 1),
                        PaymentStatus.PENDING
                ));
    }
}
