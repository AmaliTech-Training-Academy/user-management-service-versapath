package com.capstone.dto.request;

import com.capstone.validation.PasswordMatching;
import com.capstone.validation.StrongPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@PasswordMatching(message = "Password and confirmation password do not match")
public class ResetPasswordRequest {

    @NotBlank(message = "New password is required")
    @NotBlank(message = "Password is required")
    @Size(
            min = 8,
            max = 128,
            message = "Password must be between 8 and 128 characters"
    )
    @StrongPassword(message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, one special character, and be at least 8 characters long")
    private String newPassword;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;
}