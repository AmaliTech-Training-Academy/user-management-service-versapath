package com.capstone.validation;

import com.capstone.dto.request.PasswordUpdateRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchingValidator implements ConstraintValidator<PasswordMatching, PasswordUpdateRequest> {

    @Override
    public void initialize(PasswordMatching constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(PasswordUpdateRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }

        if (request.getNewPassword() == null || request.getConfirmPassword() == null) {
            return true; // Let @NotBlank handle null validation
        }

        boolean isValid = request.getNewPassword().equals(request.getConfirmPassword());
        
        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("New password and confirmation do not match")
                    .addPropertyNode("confirmPassword")
                    .addConstraintViolation();
        }
        
        return isValid;
    }
}