package com.capstone.service.impl;

import com.capstone.dto.request.AdminUserRoleUpdateRequest;
import com.capstone.dto.request.AdminUserStatusUpdateRequest;
import com.capstone.dto.request.PasswordUpdateRequest;
import com.capstone.dto.request.ProfileUpdateRequest;
import com.capstone.dto.response.PaginatedResponseDto;
import com.capstone.dto.response.UserInfoDto;
import com.capstone.dto.response.UserProfileDto;
import com.capstone.mapper.UserMapper;
import com.capstone.service.AuditService;
import com.capstone.util.PaginationUtil;
import com.capstone.exception.PasswordMismatchException;
import com.capstone.exception.UserNotFoundException;
import com.capstone.exception.UsernameAlreadyExistsException;
import com.capstone.model.ERole;
import com.capstone.model.EStatus;
import com.capstone.model.Role;
import com.capstone.model.User;
import com.capstone.repository.RoleRepository;
import com.capstone.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuditService auditService;

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
        auditService.logUserProfileUpdate(currentUser.getId().toString(), currentUser.getUsername());

        return userMapper.toUserProfileDto(updatedUser);
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
        auditService.logUserPasswordChange(currentUser.getId().toString(), currentUser.getUsername());
    }

    @Override
    public void updateMoodleUserId(UUID versapathUserId, Long moodleUserId) {
        log.info("Updating moodleUserId for versapathUserId: {} with moodleUserId: {}", versapathUserId, moodleUserId);

        User user = userRepository.findById(versapathUserId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + versapathUserId));

        user.setMoodleUserId(moodleUserId);
        userRepository.save(user);

        auditService.logSystemAction("MOODLE_ID_UPDATE", versapathUserId.toString(), "moodleUserId=" + moodleUserId);
    }

    private User getCurrentAuthenticatedUser() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userDetails.getId()));
    }

    // Admin user management methods
    @Override
    public PaginatedResponseDto<UserInfoDto> getAllUsers(int page, int size, String sortBy, String sortDirection) {
        log.info("Admin request to get all users - page: {}, size: {}, sortBy: {}, sortDirection: {}",
                page, size, sortBy, sortDirection);

        // Get current authenticated admin user ID to exclude from results
        User currentAdmin = getCurrentAuthenticatedUser();
        UUID currentAdminId = currentAdmin.getId();

        // Audit log
        String params = String.format("page=%d,size=%d,sortBy=%s,sortDirection=%s", page, size, sortBy, sortDirection);
        auditService.logBulkUserAccess(
                "VIEW_ALL_USERS",
                params,
                currentAdmin.getId().toString(),
                currentAdmin.getUsername(),
                currentAdmin.getRole().getRole().name()
        );

        // Create sort object
        Sort sort = sortDirection.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        // Create pageable
        Pageable pageable = PageRequest.of(page, size, sort);

        // Get users page excluding the current admin
        Page<User> usersPage = userRepository.findAllByIdNot(currentAdminId, pageable);

        // Convert to UserInfoDto
        Page<UserInfoDto> userInfoPage = usersPage.map(userMapper::toUserInfoDto);

        return PaginationUtil.toPaginatedResponse(userInfoPage);
    }

    @Override
    public UserInfoDto getUserById(UUID userId) {
        log.info("Admin request to get user by id: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        // Get current admin for audit
        User currentAdmin = getCurrentAuthenticatedUser();

        // Audit log
        auditService.logUserDataAccess(
                userId.toString(),
                "VIEW_USER_DETAILS",
                currentAdmin.getId().toString(),
                currentAdmin.getUsername(),
                currentAdmin.getRole().getRole().name()
        );

        return userMapper.toUserInfoDto(user);
    }

    @Override
    public UserInfoDto updateUserRole(UUID userId, AdminUserRoleUpdateRequest request) {
        log.info("Admin request to update user role - userId: {}, newRole: {}", userId, request.getRole());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        // Get current admin for audit
        User currentAdmin = getCurrentAuthenticatedUser();
        String oldRole = user.getRole().getRole().name();

        // Audit log before update
        auditService.logUserRoleUpdate(
                userId.toString(),
                oldRole,
                request.getRole(),
                currentAdmin.getId().toString(),
                currentAdmin.getUsername(),
                currentAdmin.getRole().getRole().name()
        );

        Role newRole = roleRepository.findByRole(ERole.valueOf(request.getRole()))
                .orElseThrow(() -> new RuntimeException("Role not found: " + request.getRole()));

        user.setRole(newRole);
        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(user);
        log.info("User role updated successfully - userId: {}, newRole: {}", userId, request.getRole());

        return userMapper.toUserInfoDto(updatedUser);
    }

    @Override
    public UserInfoDto updateUserStatus(UUID userId, AdminUserStatusUpdateRequest request) {
        log.info("Admin request to update user status - userId: {}, newStatus: {}", userId, request.getStatus());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        // Get current admin for audit
        User currentAdmin = getCurrentAuthenticatedUser();
        String oldStatus = user.getStatus().name();

        // Audit log before update
        auditService.logUserStatusUpdate(
                userId.toString(),
                oldStatus,
                request.getStatus(),
                currentAdmin.getId().toString(),
                currentAdmin.getUsername(),
                currentAdmin.getRole().getRole().name()
        );

        user.setStatus(EStatus.valueOf(request.getStatus()));
        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(user);
        log.info("User status updated successfully - userId: {}, newStatus: {}", userId, request.getStatus());

        return userMapper.toUserInfoDto(updatedUser);
    }

    @Override
    public int getTotalUserCount() {
        User currentAdmin = getCurrentAuthenticatedUser();
        log.info("Admin request to get total user count");

        // Audit log
        auditService.logBulkUserAccess(
                "GET_TOTAL_USER_COUNT",
                "action=count_all_users",
                currentAdmin.getId().toString(),
                currentAdmin.getUsername(),
                currentAdmin.getRole().getRole().name()
        );

        int totalCount = (int) userRepository.count();
        log.info("Total user count: {}", totalCount);
        return totalCount;
    }

    @Override
    public int getTotalLearnerCount() {
        User currentAdmin = getCurrentAuthenticatedUser();
        log.info("Admin request to get total learner count");

        // Audit log
        auditService.logBulkUserAccess(
                "GET_LEARNER_COUNT",
                "action=count_learner_users",
                currentAdmin.getId().toString(),
                currentAdmin.getUsername(),
                currentAdmin.getRole().getRole().name()
        );

        int learnerCount = userRepository.countByRole_Role(ERole.LEARNER);
        log.info("Total learner count: {}", learnerCount);
        return learnerCount;
    }
}