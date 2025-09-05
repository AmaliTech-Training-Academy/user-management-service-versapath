package com.capstone.validation;

import com.capstone.dto.request.PasswordSetupRequest;
import com.capstone.dto.request.PasswordUpdateRequest;
import com.capstone.dto.request.ResetPasswordRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchingValidator implements ConstraintValidator<PasswordMatching, Object> {

    @Override
    public void initialize(PasswordMatching constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        if (obj == null) {
            return true;
        }

        String password;
        String confirmPassword;
        String fieldName = "confirmPassword";

        // Handle different request types
        switch (obj) {
            case PasswordUpdateRequest request -> {
                password = request.getNewPassword();
                confirmPassword = request.getConfirmPassword();
            }
            case PasswordSetupRequest request -> {
                password = request.getPassword();
                confirmPassword = request.getConfirmPassword();
            }
            case ResetPasswordRequest request -> {
                password = request.getNewPassword();
                confirmPassword = request.getConfirmPassword();
            }
            default -> {
                return false;
            }
        }

        if (password == null || confirmPassword == null) {
            return true; // Let @NotBlank handle null validation
        }

        boolean isValid = password.equals(confirmPassword);

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Password and confirmation password do not match")
                    .addPropertyNode(fieldName)
                    .addConstraintViolation();
        }

        return isValid;
    }
}