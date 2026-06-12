package com.codercollie.insurance_lab_core.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = ValidQuoteDatesValidator.class)
@Target(TYPE)
@Retention(RUNTIME)
public @interface ValidQuoteDates {

    String message() default "endDate must be after startDate";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}