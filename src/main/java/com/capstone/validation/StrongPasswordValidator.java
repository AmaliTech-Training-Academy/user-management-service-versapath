package com.capstone.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;
import java.util.Set;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*[0-9].*");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>?].*");

    // Common weak passwords
    private static final Set<String> COMMON_WEAK_PASSWORDS = Set.of(
            "password", "password123", "123456", "123456789", "qwerty", "abc123",
            "111111", "123123", "admin", "letmein", "welcome", "monkey", "dragon"
    );

    @Override
    public void initialize(StrongPassword constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return false;
        }

        context.disableDefaultConstraintViolation();

        // Check for common weak passwords
        if (COMMON_WEAK_PASSWORDS.contains(password.toLowerCase())) {
            context.buildConstraintViolationWithTemplate(
                    "Password is too common and easily guessable"
            ).addConstraintViolation();
            return false;
        }

        // Check minimum length (already handled by @Size, but adding here for completeness)
        if (password.length() < 8) {
            context.buildConstraintViolationWithTemplate(
                    "Password must be at least 8 characters long"
            ).addConstraintViolation();
            return false;
        }

        // Check for uppercase letter
        if (!UPPERCASE_PATTERN.matcher(password).matches()) {
            context.buildConstraintViolationWithTemplate(
                    "Password must contain at least one uppercase letter"
            ).addConstraintViolation();
            return false;
        }

        // Check for lowercase letter
        if (!LOWERCASE_PATTERN.matcher(password).matches()) {
            context.buildConstraintViolationWithTemplate(
                    "Password must contain at least one lowercase letter"
            ).addConstraintViolation();
            return false;
        }

        // Check for digit
        if (!DIGIT_PATTERN.matcher(password).matches()) {
            context.buildConstraintViolationWithTemplate(
                    "Password must contain at least one digit"
            ).addConstraintViolation();
            return false;
        }

        // Check for special character
        if (!SPECIAL_CHAR_PATTERN.matcher(password).matches()) {
            context.buildConstraintViolationWithTemplate(
                    "Password must contain at least one special character (!@#$%^&*()_+-=[]{};':\"\\|,.<>?)"
            ).addConstraintViolation();
            return false;
        }

        // Check for repeated characters (more than 2 consecutive)
        if (hasRepeatedCharacters(password)) {
            context.buildConstraintViolationWithTemplate(
                    "Password cannot contain more than 2 consecutive identical characters"
            ).addConstraintViolation();
            return false;
        }

        return true;
    }

    private boolean hasRepeatedCharacters(String password) {
        for (int i = 0; i < password.length() - 2; i++) {
            if (password.charAt(i) == password.charAt(i + 1) &&
                    password.charAt(i + 1) == password.charAt(i + 2)) {
                return true;
            }
        }
        return false;
    }
}