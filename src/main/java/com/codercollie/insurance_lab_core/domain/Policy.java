package com.codercollie.insurance_lab_core.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public record Policy(
        String policyNumber,
        Customer customer,
        Product product,
        List<Coverage> coverages,
        Premium premium,
        LocalDate startDate,
        LocalDate endDate,
        PolicyStatus status
) {

    public Policy {
        if (policyNumber == null || policyNumber.isBlank()) {
            throw new IllegalArgumentException("policyNumber is required");
        }

        Objects.requireNonNull(customer, "customer is required");
        Objects.requireNonNull(product, "product is required");
        Objects.requireNonNull(premium, "premium is required");
        Objects.requireNonNull(startDate, "startDate is required");
        Objects.requireNonNull(endDate, "endDate is required");
        Objects.requireNonNull(status, "status is required");

        if (coverages == null || coverages.isEmpty()) {
            throw new IllegalArgumentException("at least one coverage is required");
        }

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate cannot be before startDate");
        }

        coverages = List.copyOf(coverages);
    }
}
