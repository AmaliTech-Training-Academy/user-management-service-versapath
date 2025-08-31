package com.capstone.service.impl;

import com.capstone.dto.request.PasswordUpdateRequest;
import com.capstone.dto.request.ProfileUpdateRequest;
import com.capstone.dto.response.UserProfileDto;
import com.capstone.exception.PasswordMismatchException;
import com.capstone.exception.UserNotFoundException;
import com.capstone.exception.UsernameAlreadyExistsException;
import com.capstone.model.User;
import com.capstone.repository.UserRepository;
import com.capstone.security.CustomUserDetails;
import com.capstone.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserProfileDto updateUserProfile(ProfileUpdateRequest request) {
        User currentUser = getCurrentAuthenticatedUser();
        log.info("Updating profile for user: {}", currentUser.getEmail());

        // Check if username is already taken by another user
        if (!currentUser.getUsername().equals(request.getUsername())) {
            Optional<User> existingUser = userRepository.findByUsername(request.getUsername());
            if (existingUser.isPresent()) {
                log.warn("Username already exists: {}", request.getUsername());
                throw new UsernameAlreadyExistsException("Username '" + request.getUsername() + "' is already taken");
            }
        }

        // Update user profile
        currentUser.setFirstName(request.getFirstName());
        currentUser.setLastName(request.getLastName());
        currentUser.setUsername(request.getUsername());
        currentUser.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(currentUser);
        log.info("Profile updated successfully for user: {}", updatedUser.getEmail());

        return buildUserProfileDto(updatedUser);
    }

    @Override
    public void updateUserPassword(PasswordUpdateRequest request) {
        User currentUser = getCurrentAuthenticatedUser();
        log.info("Password update request for user: {}", currentUser.getEmail());

        // Validate password confirmation
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException("New password and confirmation do not match");
        }

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
            log.warn("Invalid current password for user: {}", currentUser.getEmail());
            throw new BadCredentialsException("Current password is incorrect");
        }

        // Update password
        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        currentUser.setUpdatedAt(LocalDateTime.now());

        userRepository.save(currentUser);
        log.info("Password updated successfully for user: {}", currentUser.getEmail());
    }

    private User getCurrentAuthenticatedUser() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userDetails.getId()));
    }

    private UserProfileDto buildUserProfileDto(User user) {
        return UserProfileDto.builder()
                .id(user.getId().toString())
                .email(user.getEmail())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().getRole().name().replace("ROLE_", ""))
                .build();
    }
}