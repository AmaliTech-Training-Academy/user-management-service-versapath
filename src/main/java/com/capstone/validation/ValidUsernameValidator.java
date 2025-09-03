package com.capstone.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;
import java.util.Set;

public class ValidUsernameValidator implements ConstraintValidator<ValidUsername, String> {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9._-]*[a-zA-Z0-9]$|^[a-zA-Z0-9]$");

    // Reserved usernames that should not be allowed
    private static final Set<String> RESERVED_USERNAMES = Set.of(
            "admin", "administrator", "root", "user", "test", "demo", "guest", "public", "private",
            "api", "www", "mail", "email", "support", "help", "info", "contact", "about",
            "login", "register", "signin", "signup", "auth", "oauth", "null", "undefined"
    );

    @Override
    public void initialize(ValidUsername constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String username, ConstraintValidatorContext context) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        username = username.trim().toLowerCase();

        // Check pattern
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            return false;
        }

        // Check for reserved usernames
        if (RESERVED_USERNAMES.contains(username)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Username '" + username + "' is reserved and cannot be used"
            ).addConstraintViolation();
            return false;
        }

        // Check for consecutive dots or special characters
        if (username.contains("..") || username.contains("--") || username.contains("__")) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Username cannot contain consecutive special characters"
            ).addConstraintViolation();
            return false;
        }

        return true;
    }
}