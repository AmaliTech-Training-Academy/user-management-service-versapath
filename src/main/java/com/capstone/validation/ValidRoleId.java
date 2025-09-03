package com.capstone.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ValidRoleIdValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidRoleId {
    String message() default "Invalid role ID";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}