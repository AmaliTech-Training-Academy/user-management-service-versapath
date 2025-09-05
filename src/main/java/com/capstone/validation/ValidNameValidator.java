package com.capstone.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class ValidNameValidator implements ConstraintValidator<ValidName, String> {

    // Allow letters, spaces, hyphens, apostrophes, and some accented characters
    private static final Pattern NAME_PATTERN = Pattern.compile(
            "^[a-zA-ZÀ-ÿĀ-žА-я\\u0100-\\u017F\\u1E00-\\u1EFF\\s'-]+$"
    );

    @Override
    public void initialize(ValidName constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String name, ConstraintValidatorContext context) {
        if (name == null) {
            return false;
        }

        String trimmedName = name.trim();

        if (trimmedName.isEmpty()) {
            return false;
        }

        // Check pattern
        if (!NAME_PATTERN.matcher(trimmedName).matches()) {
            return false;
        }

        // Additional validations
        // No consecutive spaces
        if (trimmedName.contains("  ")) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Name cannot contain consecutive spaces"
            ).addConstraintViolation();
            return false;
        }

        // No leading/trailing special characters
        if (trimmedName.startsWith("-") || trimmedName.startsWith("'") ||
                trimmedName.endsWith("-") || trimmedName.endsWith("'")) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Name cannot start or end with special characters"
            ).addConstraintViolation();
            return false;
        }

        return true;
    }
}