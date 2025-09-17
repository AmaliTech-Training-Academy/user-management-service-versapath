package com.capstone.service;

import com.capstone.dto.request.AdminUserRoleUpdateRequest;
import com.capstone.dto.request.AdminUserStatusUpdateRequest;
import com.capstone.dto.request.PasswordUpdateRequest;
import com.capstone.dto.request.ProfileUpdateRequest;
import com.capstone.dto.response.PaginatedResponseDto;
import com.capstone.dto.response.UserInfoDto;
import com.capstone.dto.response.UserProfileDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface UserManagementService {
    UserProfileDto updateUserProfile(ProfileUpdateRequest request, MultipartFile profilePicture);
    void updateUserPassword(PasswordUpdateRequest request);
    void updateMoodleUserId(UUID versapathUserId, Long moodleUserId);
    void deleteProfilePicture();
    
    // Admin user management methods
    PaginatedResponseDto<UserInfoDto> getAllUsers(int page, int size, String sortBy, String sortDirection);
    UserInfoDto getUserById(UUID userId);
    UserInfoDto updateUserRole(UUID userId, AdminUserRoleUpdateRequest request);
    UserInfoDto updateUserStatus(UUID userId, AdminUserStatusUpdateRequest request);
    long getTotalUserCount();
    long getTotalLearnerCount();
}