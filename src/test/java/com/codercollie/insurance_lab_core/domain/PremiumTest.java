package com.codercollie.insurance_lab_core.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;

class PremiumTest {

    @Test
    void rejectsNegativeAmount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Premium(
                        new BigDecimal("-2.00"),
                        LocalDate.of(2026, 1, 1))
        );
    }

    @Test
    void rejectsZeroAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> new Premium(
                        BigDecimal.ZERO,
                        LocalDate.of(2026, 1, 1)));
    }

    @Test
    void rejectsMissingDueDate() {
        assertThrows(NullPointerException.class,
                () -> new Premium(
                        new BigDecimal("1.00"),
                        null
                ));
    }
}
