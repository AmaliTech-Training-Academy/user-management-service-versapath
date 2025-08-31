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
@Schema(description = "Login response with tokens and user information")
public class LoginResponseDto {

    @Schema(description = "Basic user information")
    private UserInfoDto item;

    @Schema(description = "Token type", example = "Bearer")
    private String tokenType;

    @Schema(description = "JWT access token for API authentication")
    private String accessToken;

    @Schema(description = "Access token expiration time in seconds from now")
    private long expiresIn;

}
