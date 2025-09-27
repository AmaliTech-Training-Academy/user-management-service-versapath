package com.capstone.dto.request;

import com.capstone.validation.ValidRoleId;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload for user invitation by admin")
public class UserRegistrationRequest {

    @Schema(
            description = "User's email address for invitation",
            example = "john.doe@company.com"
    )
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

    @Schema(
            description = "Role ID to assign to the invited user",
            example = "550e8400-e29b-41d4-a716-446655440000"
    )
    @NotNull(message = "Role ID is required")
    @ValidRoleId(message = "Invalid role ID provided")
    private UUID roleId;

    @Schema(description = "Specialization IDs for mentor role (ignored for other roles)",
            example = "[\"550e8400-e29b-41d4-a716-446655440000\", \"550e8400-e29b-41d4-a716-446655440001\"]")
    @Valid
    private List<UUID> specializationIds;
}