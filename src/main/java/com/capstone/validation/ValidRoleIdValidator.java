package com.capstone.validation;

import com.capstone.repository.RoleRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ValidRoleIdValidator implements ConstraintValidator<ValidRoleId, UUID> {

    private final RoleRepository roleRepository;

    @Override
    public void initialize(ValidRoleId constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(UUID roleId, ConstraintValidatorContext context) {
        if (roleId == null) {
            return false;
        }

        try {
            return roleRepository.existsById(roleId);
        } catch (Exception e) {
            return false;
        }
    }
}