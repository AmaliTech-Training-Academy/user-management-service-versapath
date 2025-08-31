package com.capstone.service;

import com.capstone.dto.request.PasswordUpdateRequest;
import com.capstone.dto.request.ProfileUpdateRequest;
import com.capstone.dto.response.UserProfileDto;

public interface UserManagementService {
    UserProfileDto updateUserProfile(ProfileUpdateRequest request);
    void updateUserPassword(PasswordUpdateRequest request);
}