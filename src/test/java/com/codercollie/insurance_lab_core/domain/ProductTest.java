package com.codercollie.insurance_lab_core.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductTest {

    @Test
    void rejectMissingProductCode() {
        assertThrows(IllegalArgumentException.class,

                () -> new Product(
                        null,
                        "bingo"
                ));
    }

    @Test
    void rejectsMissingName() {
        assertThrows(IllegalArgumentException.class,

                () -> new Product(
                        "bingo",
                        null
                ));
    }

}
