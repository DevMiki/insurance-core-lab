package com.codercollie.insurance_lab_core.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomerTest {

    @Test
    void rejectMissingCustomerCode() {
        assertThrows(IllegalArgumentException.class,
                () -> new Customer(
                        null,
                        "bingo bongo"
                ));
    }

    @Test
    void rejectsMissingFullName() {
        assertThrows(IllegalArgumentException.class,
                () -> new Customer(
                        "codice bingo",
                        null));
    }

}
