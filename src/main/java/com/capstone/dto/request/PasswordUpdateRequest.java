package com.capstone.dto.request;

import com.capstone.validation.PasswordMatching;
import com.capstone.validation.StrongPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to update user password")
@PasswordMatching
public class PasswordUpdateRequest {

    @Schema(description = "Current password for verification", example = "CurrentPass123!")
    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @Schema(description = "New password", example = "NewPass123!")
    @NotBlank(message = "New password is required")
    @Size(
            min = 8,
            max = 128,
            message = "Password must be between 8 and 128 characters"
    )
    @StrongPassword(message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, one special character, and be at least 8 characters long")
    private String newPassword;

    @Schema(description = "Confirmation of new password", example = "NewPass123!")
    @NotBlank(message = "Password confirmation is required")
    private String confirmPassword;
}