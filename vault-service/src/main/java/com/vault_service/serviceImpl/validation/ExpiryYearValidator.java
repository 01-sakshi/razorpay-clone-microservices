package com.vault_service.serviceImpl.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.Year;

public class ExpiryYearValidator implements ConstraintValidator<ExpiryYear, Integer> {
    /** Accepts only non-null expiry years equal to or later than the current calendar year. */
    @Override
    public boolean isValid(Integer expiryYear, ConstraintValidatorContext context) {
        if (expiryYear == null) return false;
        Year now = Year.now();
        return now.getValue() <= expiryYear;
    }
}
