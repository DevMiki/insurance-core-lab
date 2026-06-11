package com.codercollie.insurance_lab_core.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PremiumCalculatorTest {

    @Test
    void calculatesPremiumFromCoveragesDurationAndTax() {
        PremiumCalculator calculator = new SimplePremiumCalculator();

        PremiumCalculationResult result = calculator.calculate(
                List.of(
                        new CoveragePrice(
                                "FIRE",
                                new BigDecimal("100.00")),
                        new CoveragePrice(
                                "THEFT",
                                new BigDecimal("50.00")
                        )
                ),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 11)
        );

        assertEquals(new BigDecimal("1500.00"), result.netPremium());
        assertEquals(new BigDecimal("330.00"), result.taxAmount());
        assertEquals(new BigDecimal("1830.00"), result.totalAmount());
    }

    @Test
    void rejectsEmptyCoverages() {
        PremiumCalculator calculator = new SimplePremiumCalculator();

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(
                        List.of(),
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 1, 11)
                )
        );
    }

    @Test
    void rejectsEndDateBeforeStartDate() {
        PremiumCalculator calculator = new SimplePremiumCalculator();

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(
                        List.of(new CoveragePrice("FIRE", new BigDecimal("100.00"))),
                        LocalDate.of(2026, 1, 11),
                        LocalDate.of(2026, 1, 1)
                )
        );
    }
}
