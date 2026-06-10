package com.codercollie.insurance_lab_core.dto.customer;

public record CustomerResponse(
        Long id,
        String customerCode,
        String fullName
) {
}
