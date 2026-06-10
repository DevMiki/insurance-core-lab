package com.codercollie.insurance_lab_core.dto.customer;

import jakarta.validation.constraints.NotBlank;

public record CreateCustomerRequest(
        @NotBlank(message = "customerCode is required")
        String customerCode,

        @NotBlank(message = "fullName is required")
        String fullName){

}
