package com.capstone.controller;

import com.capstone.dto.request.ForgotPasswordRequest;
import com.capstone.dto.request.LoginRequestDto;
import com.capstone.dto.request.ResetPasswordRequest;
import com.capstone.dto.response.*;
import com.capstone.service.AuthenticationService;
import com.capstone.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Authentication", description = "User authentication and authorization endpoints")
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/login")
    @Operation(
            summary = "User Login",
            description = "Authenticate user with email and password, returns JWT access token and sets refresh token in HttpOnly cookie"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "403", description = "Account disabled or locked")
    })
    public ResponseEntity<ApiResponseDto<LoginResponseDto>> login(
            @Valid @RequestBody LoginRequestDto loginRequest,
            HttpServletResponse response) {
        log.info("Login request for email: {}", loginRequest.getEmail());

        LoginResponseDto loginResponse = authenticationService.login(loginRequest, response);

        return ResponseEntity.ok(
                ApiResponseDto.success(loginResponse, "Login successful")
        );
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh Access Token",
            description = "Generate new access token using refresh token from HttpOnly cookie"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid refresh token"),
            @ApiResponse(responseCode = "401", description = "Expired or invalid refresh token")
    })
    public ResponseEntity<ApiResponseDto<RefreshTokenResponseDto>> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) {

        log.info("Token refresh request received");

        RefreshTokenResponseDto refreshResponse = authenticationService.refreshToken(request, response);

        return ResponseEntity.ok(
                ApiResponseDto.success(refreshResponse, "Token refreshed successfully")
        );
    }

    @PostMapping("/logout")
    @Operation(
            summary = "User Logout",
            description = "Logout current user, blacklist access token, clear session and remove refresh token cookie"
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful"),
            @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    public ResponseEntity<ApiResponseDto<LogoutResponseDto>> logout(
            HttpServletResponse response,
            HttpServletRequest request) {

        log.info("Logout request received");

        LogoutResponseDto logoutResponse = authenticationService.logout(response, request);

        return ResponseEntity.ok(
                ApiResponseDto.success(logoutResponse, "Logout successful")
        );
    }

    @GetMapping("/me")
    @Operation(
            summary = "Get Current User",
            description = "Get information about currently authenticated user"
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User information retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    public ResponseEntity<ApiResponseDto<UserProfileDto>> getCurrentUser() {

            UserProfileDto response = authenticationService.getCurrentUser();

            return ResponseEntity.ok(
                    ApiResponseDto.success(response, "User information retrieved successfully")
            );

        }

    @PostMapping("/forgot-password")
    @Operation(
            summary = "Forgot Password",
            description = "Send password reset link to user's email if account exists and is active"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset process initiated"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "429", description = "Too many requests")
    })
    public ResponseEntity<ApiResponseDto<PasswordResetResponse>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        log.info("Forgot password request for email: {}", request.getEmail());

        PasswordResetResponse response = passwordResetService.processForgotPassword(request);

        return ResponseEntity.ok(
                ApiResponseDto.success(response)
        );
    }

    @PostMapping("/reset-password")
    @Operation(
            summary = "Reset Password",
            description = "Reset user password using valid reset token"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset successful"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token")
    })
    public ResponseEntity<ApiResponseDto<PasswordResetResponse>> resetPassword(
            @RequestParam("reset") String token, @Valid @RequestBody ResetPasswordRequest request) {

        PasswordResetResponse response = passwordResetService.resetPassword(token, request);

        return ResponseEntity.ok(
                ApiResponseDto.success(response)
        );
    }
}
