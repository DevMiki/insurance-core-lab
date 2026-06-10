package com.codercollie.insurance_lab_core.dto.product;

import jakarta.validation.constraints.NotBlank;

public record CreateProductRequest(
        @NotBlank(message = "productCode is required")
        String productCode,

        @NotBlank(message = "name is required")
        String name
) {
}
