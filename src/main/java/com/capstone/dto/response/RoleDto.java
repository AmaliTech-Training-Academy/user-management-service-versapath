package com.capstone.dto.response;

import com.capstone.model.ERole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Role information")
public class RoleDto {

    @Schema(description = "Role ID", example = "1")
    private UUID id;

    @Schema(description = "Role name", example = "ADMIN", allowableValues = {"ADMIN", "MANAGER", "MENTOR", "LEARNER"})
    private ERole name;

    @Schema(description = "Role description", example = "System administrator with full access")
    private String description;
}