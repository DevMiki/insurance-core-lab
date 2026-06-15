package com.codercollie.insurance_lab_core.validation;

import com.codercollie.insurance_lab_core.dto.quote.CreateQuoteRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidQuoteDatesValidatorTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void acceptsEndDateAfterStartDate() {
        CreateQuoteRequest request = new CreateQuoteRequest(
                1L,
                2L,
                List.of(10L),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 11)
        );

        Set<ConstraintViolation<CreateQuoteRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void rejectsEndDateEqualToStartDate() {
        CreateQuoteRequest request = new CreateQuoteRequest(
                1L,
                2L,
                List.of(10L),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 1)
        );

        Set<ConstraintViolation<CreateQuoteRequest>> violations = validator.validate(request);

        assertTrue(violations.stream()
                .anyMatch(violation -> violation.getMessage().equals("endDate must be after startDate"))
        );
    }

    @Test
    void rejectsEndDateBeforeStartDate() {
        CreateQuoteRequest request = new CreateQuoteRequest(
                1L,
                2L,
                List.of(10L),
                LocalDate.of(2026, 1, 11),
                LocalDate.of(2026, 1, 1)
        );

        Set<ConstraintViolation<CreateQuoteRequest>> violations = validator.validate(request);

        assertTrue(violations.stream()
                .anyMatch(violation -> violation.getMessage().equals("endDate must be after startDate"))
        );
    }

}
