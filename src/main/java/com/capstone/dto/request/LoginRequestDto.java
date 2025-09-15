package com.capstone.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Login request payload")
public class LoginRequestDto {

    @Schema(description = "User's email address", example = "user@example.com")
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

    @NotBlank(message = "Password is required")
    @Schema(description = "User's password", example = "password123")
    private String password;
}
