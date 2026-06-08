package com.codercollie.insurance_lab_core.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomerTest {

    @Test
    void rejectsMissingCustomerCode() {
        assertThrows(IllegalArgumentException.class,
                () -> new Customer(
                        null,
                        "Mario Rossi"
                ));
    }

    @Test
    void rejectsMissingFullName() {
        assertThrows(IllegalArgumentException.class,
                () -> new Customer(
                        "CUS-001",
                        null));
    }
}
