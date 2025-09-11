package com.capstone.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Simplified login response with basic user information")
public class LoginResponseDto {

    @Schema(description = "User's unique identifier")
    private String userId;

    @Schema(description = "User's email address")
    private String email;

    @Schema(description = "User's email address")
    private String firstName;

    @Schema(description = "User's email address")
    private String lastName;

    @Schema(description = "User's username")
    private String username;

    @Schema(description = "User's role name", example = "ADMIN")
    private String role;
}