package com.capstone.controller;

import com.capstone.dto.request.PasswordUpdateRequest;
import com.capstone.dto.request.ProfileUpdateRequest;
import com.capstone.dto.response.ApiResponseDto;
import com.capstone.dto.response.UserProfileDto;
import com.capstone.service.UserManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "User Management", description = "User profile management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class UserManagementController {

    private final UserManagementService userManagementService;

    @PatchMapping("/profile")
    @Operation(
            summary = "Update User Profile",
            description = "Update the profile information (name fields and username) of the currently authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data or username already exists"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "409", description = "Username already exists")
    })
    public ResponseEntity<ApiResponseDto<UserProfileDto>> updateUserProfile(
            @Valid @RequestBody ProfileUpdateRequest request) {
        log.info("Update user profile request received");

        UserProfileDto updatedProfile = userManagementService.updateUserProfile(request);

        return ResponseEntity.ok(
                ApiResponseDto.success(updatedProfile, "Profile updated successfully")
        );
    }

    @PatchMapping("/password")
    @Operation(
            summary = "Update User Password",
            description = "Change the password of the currently authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data or password mismatch"),
            @ApiResponse(responseCode = "401", description = "User not authenticated or current password incorrect"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponseDto<Void>> updateUserPassword(
            @Valid @RequestBody PasswordUpdateRequest request) {
        log.info("Update user password request received");

        userManagementService.updateUserPassword(request);

        return ResponseEntity.ok(
                ApiResponseDto.success(null, "Password updated successfully")
        );
    }
}