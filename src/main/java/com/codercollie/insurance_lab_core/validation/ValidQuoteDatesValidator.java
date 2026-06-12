package com.codercollie.insurance_lab_core.validation;

import com.codercollie.insurance_lab_core.dto.quote.CreateQuoteRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidQuoteDatesValidator implements
        ConstraintValidator<ValidQuoteDates, CreateQuoteRequest> {

    @Override
    public boolean isValid(CreateQuoteRequest request,
                           ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }

        if (request.startDate() == null || request.endDate() == null) {
            return true;
        }

        return request.endDate().isAfter(request.startDate());
    }
}