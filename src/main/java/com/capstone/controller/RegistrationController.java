package com.capstone.controller;

import com.capstone.dto.request.PasswordSetupRequest;
import com.capstone.dto.request.UserRegistrationRequest;
import com.capstone.dto.response.*;
import com.capstone.service.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/register")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Registration", description = "Endpoints for user registration and invitation management")
public class RegistrationController {
    private final RegistrationService registrationService;

    @PostMapping("/invite-user")
    @Operation(
            summary = "Invite a new user",
            description = "Admin invites a user by providing their email and role. Creates a pending user and sends invitation email with registration link."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User invited successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
            @ApiResponse(responseCode = "409", description = "User already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<UserRegistrationResponse>> inviteUser(
            @Valid @RequestBody UserRegistrationRequest request) {

        log.info("Admin inviting user with email: {}", request.getEmail());
        ApiResponseDto<UserRegistrationResponse> response = registrationService.inviteUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/complete-registration")
    @Operation(
            summary = "Complete user registration",
            description = "User completes registration by providing personal details and password using the token from invitation email."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registration completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid Request"),
            @ApiResponse(responseCode = "409", description = "Username already taken"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponseDto<PasswordSetupResponse>> completeRegistration(
            @RequestParam("invite") String token, @Valid @RequestBody PasswordSetupRequest request) {

        log.info("Processing registration completion");
        ApiResponseDto<PasswordSetupResponse> response = registrationService.completeRegistration(token, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-invitation")
    @Operation(
            summary = "Resend invitation email",
            description = "Admin can resend invitation email for a user who is in pending status. Generates a new token."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invitation resent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<String>> resendInvitation(
            @Parameter(description = "Email address to resend invitation to", required = true)
            @RequestParam String email) {

        log.info("Admin resending invitation to email: {}", email);
        ApiResponseDto<String> response = registrationService.resendInvitation(email);
        return ResponseEntity.ok(response);
    }

}
