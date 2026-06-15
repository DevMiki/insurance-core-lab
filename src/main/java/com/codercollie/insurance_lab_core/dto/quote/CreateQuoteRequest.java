package com.codercollie.insurance_lab_core.dto.quote;

import com.codercollie.insurance_lab_core.validation.ValidQuoteDates;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

@ValidQuoteDates
public record CreateQuoteRequest(
        @NotNull(message = "productId is required")
        Long productId,

        @NotNull(message = "customerId is required")
        Long customerId,

        @NotEmpty(message = "coverageIds are required")
        List<Long> coverageIds,

        @NotNull(message = "startDate is required")
        LocalDate startDate,

        @NotNull(message = "endDate is required")
        LocalDate endDate
) {
}
