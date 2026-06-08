package com.codercollie.insurance_lab_core.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;

class CoverageTest {

    @Test
    void rejectsMissingCoverageCode() {
        assertThrows(IllegalArgumentException.class,
                () -> new Coverage(
                        null,
                        "Fire damage",
                        new BigDecimal("2.00")
                ));
    }

    @Test
    void rejectsMissingDescription() {
        assertThrows(IllegalArgumentException.class,
                () -> new Coverage(
                        "FIRE",
                        null,
                        new BigDecimal("2.00")
                ));
    }

    @Test
    void rejectsMissingInsuredAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> new Coverage(
                        "FIRE",
                        "Fire damage",
                        null
                ));
    }

    @Test
    void rejectsZeroInsuredAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> new Coverage(
                        "FIRE",
                        "Fire damage",
                        BigDecimal.ZERO
                ));
    }
}
