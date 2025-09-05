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
@Schema(description = "Request to update user status by admin")
public class AdminUserStatusUpdateRequest {

    @Schema(description = "User's new status", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE"})
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(ACTIVE|INACTIVE)$", message = "Status must be one of: ACTIVE, INACTIVE")
    private String status;
}