package com.codercollie.insurance_lab_core.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public record Premium(BigDecimal amount, LocalDate dueDate) {

    public Premium {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("premium amount must be greater than zero");
        }

        Objects.requireNonNull(dueDate, "dueDate must not be null");
    }
}
