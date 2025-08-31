package com.capstone.dto.request;

import com.capstone.validation.PasswordMatching;
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
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String newPassword;

    @Schema(description = "Confirmation of new password", example = "NewPass123!")
    @NotBlank(message = "Password confirmation is required")
    private String confirmPassword;
}