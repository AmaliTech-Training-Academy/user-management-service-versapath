package com.capstone.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordMatchingValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordMatching {
    String message() default "Password and confirmation do not match";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}