package com.codercollie.insurance_lab_core.domain;

public record Customer(String customerCode,
                       String fullName) {

    public Customer {
        if(customerCode == null || customerCode.isBlank()) {
            throw new IllegalArgumentException("customerCode is required");
        }

        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("fullName is required");
        }
    }

}
