package com.capstone.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to update user role by admin")
public class AdminUserRoleUpdateRequest {

    @Schema(description = "User's new role", example = "LEARNER", allowableValues = {"ADMIN", "MANAGER", "LEARNER", "MENTOR"})
    @NotBlank(message = "Role is required")
    @Pattern(regexp = "^(ADMIN|MANAGER|LEARNER|MENTOR)$", message = "Role must be one of: ADMIN, MANAGER, LEARNER, MENTOR")
    private String role;
}