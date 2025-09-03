package com.capstone.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordRequest {

    @NotBlank(message = "Email is required")
    @Size(
            min = 5,
            max = 100,
            message = "Email must be between 5 and 100 characters"
    )
    @Pattern(
            regexp = "^(?!.*\\.{2})[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            message = "Email format is invalid - consecutive dots, invalid characters, or malformed domain"
    )
    private String email;
}