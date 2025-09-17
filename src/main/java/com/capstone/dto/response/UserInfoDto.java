package com.capstone.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User information returned in login response")
public class UserInfoDto {

    @Schema(description = "User's unique identifier")
    private String id;

    @Schema(description = "User's email address")
    private String email;

    @Schema(description = "User's username")
    private String username;

    @Schema(description = "User's first name")
    private String firstName;

    @Schema(description = "User's last name")
    private String lastName;

    @Schema(description = "User's phone number")
    private String phoneNumber;

    @Schema(description = "User's role in the system")
    private String role;

    @Schema(description = "User's account status")
    private String status;

    @Schema(description = "Account creation date")
    private LocalDateTime createdAt;

    @Schema(description = "Last account update date")
    private LocalDateTime updatedAt;
}
