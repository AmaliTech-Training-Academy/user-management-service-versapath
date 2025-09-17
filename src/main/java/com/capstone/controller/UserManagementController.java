package com.capstone.controller;

import com.capstone.dto.request.AdminUserRoleUpdateRequest;
import com.capstone.dto.request.AdminUserStatusUpdateRequest;
import com.capstone.dto.request.PasswordUpdateRequest;
import com.capstone.dto.request.ProfileUpdateRequest;
import com.capstone.dto.response.ApiResponseDto;
import com.capstone.dto.response.PaginatedResponseDto;
import com.capstone.dto.response.UserInfoDto;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "User Management", description = "User profile management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class UserManagementController {

    private final UserManagementService userManagementService;

    @PatchMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Update User Profile",
            description = "Update profile information and optionally upload a new profile picture"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data or username already exists"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "409", description = "Username already exists"),
            @ApiResponse(responseCode = "500", description = "File upload failed")
    })
    public ResponseEntity<ApiResponseDto<UserProfileDto>> updateUserProfile(
            @RequestPart("profile") @Valid ProfileUpdateRequest request,
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture) {

        log.info("Update user profile request received with picture: {}",
                profilePicture != null ? "yes" : "no");

        UserProfileDto updatedProfile = userManagementService.updateUserProfile(request, profilePicture);

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

    @PatchMapping("/profile-picture")
    @Operation(
            summary = "Delete Profile Picture",
            description = "Remove the current profile picture for the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile picture deleted successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "No profile picture found")
    })
    public ResponseEntity<ApiResponseDto<Void>> deleteProfilePicture() {
        log.info("Profile picture delete request received");

        userManagementService.deleteProfilePicture();

        return ResponseEntity.ok(
                ApiResponseDto.success(null, "Profile picture deleted successfully")
        );
    }

    // Admin endpoints
    @GetMapping
    @Operation(
            summary = "Get All Users",
            description = "Retrieve paginated list of all users with sorting options (Admin only)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<PaginatedResponseDto<UserInfoDto>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "firstName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        
        log.info("Admin request to get all users - page: {}, size: {}, sortBy: {}, sortDirection: {}", 
                page, size, sortBy, sortDirection);

        PaginatedResponseDto<UserInfoDto> users = userManagementService.getAllUsers(page, size, sortBy, sortDirection);

        return ResponseEntity.ok(
                ApiResponseDto.success(users, "Users retrieved successfully")
        );
    }

    @GetMapping("/{userId}")
    @Operation(
            summary = "Get User by ID",
            description = "Retrieve specific user details by ID (Admin only)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<UserInfoDto>> getUserById(@PathVariable UUID userId) {
        log.info("Admin request to get user by id: {}", userId);

        UserInfoDto user = userManagementService.getUserById(userId);

        return ResponseEntity.ok(
                ApiResponseDto.success(user, "User retrieved successfully")
        );
    }

    @PatchMapping("/{userId}/role")
    @Operation(
            summary = "Update User Role",
            description = "Update user's role (Admin only)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User role updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid role or input data"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<UserInfoDto>> updateUserRole(
            @PathVariable UUID userId,
            @Valid @RequestBody AdminUserRoleUpdateRequest request) {
        
        log.info("Admin request to update user role - userId: {}, newRole: {}", userId, request.getRole());

        UserInfoDto updatedUser = userManagementService.updateUserRole(userId, request);

        return ResponseEntity.ok(
                ApiResponseDto.success(updatedUser, "User role updated successfully")
        );
    }

    @PatchMapping("/{userId}/status")
    @Operation(
            summary = "Update User Status",
            description = "Update user's status (Admin only)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status or input data"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<UserInfoDto>> updateUserStatus(
            @PathVariable UUID userId,
            @Valid @RequestBody AdminUserStatusUpdateRequest request) {
        
        log.info("Admin request to update user status - userId: {}, newStatus: {}", userId, request.getStatus());

        UserInfoDto updatedUser = userManagementService.updateUserStatus(userId, request);

        return ResponseEntity.ok(
                ApiResponseDto.success(updatedUser, "User status updated successfully")
        );
    }
    @GetMapping("/count")
    @Operation(
            summary = "Get Total User Count",
            description = "Retrieve total count of all users in the system (Admin only)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User count retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<Long>> getTotalUserCount() {
        log.info("Admin request to get total user count");

        long userCount = userManagementService.getTotalUserCount();

        return ResponseEntity.ok(
                ApiResponseDto.success(userCount, "Total user count retrieved successfully")
        );
    }

    @GetMapping("/learners/count")
    @Operation(
            summary = "Get Total Learner Count",
            description = "Retrieve total count of all learners (users with USER role) in the system (Admin only)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Learner count retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<Long>> getTotalLearnerCount() {
        log.info("Admin request to get total learner count");

        long learnerCount = userManagementService.getTotalLearnerCount();

        return ResponseEntity.ok(
                ApiResponseDto.success(learnerCount, "Total learner count retrieved successfully")
        );
    }
}