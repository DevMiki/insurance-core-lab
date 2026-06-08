package com.codercollie.insurance_lab_core.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductTest {

    @Test
    void rejectsMissingProductCode() {
        assertThrows(IllegalArgumentException.class,
                () -> new Product(
                        null,
                        "Home Insurance"
                ));
    }

    @Test
    void rejectsMissingName() {
        assertThrows(IllegalArgumentException.class,
                () -> new Product(
                        "HOME",
                        null
                ));
    }
}
