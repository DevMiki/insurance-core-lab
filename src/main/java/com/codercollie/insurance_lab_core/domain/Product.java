package com.codercollie.insurance_lab_core.domain;

public record Product(String productCode, String name) {


    public Product {
        if (productCode == null || productCode.isBlank()) {
            throw new IllegalArgumentException("productCode is required");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
    }
}
