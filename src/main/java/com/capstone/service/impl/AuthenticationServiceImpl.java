package com.capstone.service.impl;

import com.capstone.dto.response.*;
import com.capstone.model.EStatus;
import com.capstone.model.User;
import com.capstone.service.AuthenticationService;
import com.capstone.repository.UserRepository;
import com.capstone.security.CustomUserDetails;
import com.capstone.service.PreSignedUrlService;
import com.capstone.service.SessionManagementService;
import com.capstone.util.CookieUtil;
import com.capstone.util.JwtAuthUtil;
import com.capstone.dto.request.LoginRequestDto;
import com.capstone.mapper.UserMapper;
import com.capstone.service.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtAuthUtil jwtAuthUtil;
    private final CookieUtil cookieUtil;
    private final UserMapper userMapper;
    private final TokenBlacklistService tokenBlacklistService;
    private final SessionManagementService sessionManagementService;
    private final PreSignedUrlService preSignedUrlService;

    @Value("${APP_SESSION_MAX_CONCURRENT_SESSIONS:3}")
    private int maxConcurrentSessions;

    /**
     * Authenticate user and generate JWT tokens
     */
    @Override
    public LoginResponseDto login(LoginRequestDto loginRequest, HttpServletResponse response, HttpServletRequest request) {
        log.info("Login attempt for email: {}", loginRequest.getEmail());

            // Authenticate user credentials
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            log.info("User authenticated successfully: {}", userDetails.getUsername());

            // Get client information
            String ipAddress = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");

            // Check if user has too many sessions from DIFFERENT devices
            int currentSessions = sessionManagementService.getActiveSessionCount(userDetails.getId());

            // Only block if user has max sessions AND this is a different device
            if (currentSessions >= maxConcurrentSessions) {
                // Check if any existing session is from same device
                boolean hasSameDeviceSession = sessionManagementService.getUserSessions(userDetails.getId())
                        .stream()
                        .anyMatch(session -> session.getIpAddress().equals(ipAddress) &&
                                java.util.Objects.equals(session.getUserAgent(), userAgent));

                if (!hasSameDeviceSession) {
                    log.warn("User {} attempted login from new device but has {} active sessions from different devices",
                            userDetails.getEmail(), currentSessions);
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            String.format("Maximum device limit reached (%d devices). Login from new device blocked.", maxConcurrentSessions)
                    );
                }
            }

            // Generate tokens
            String accessToken = jwtAuthUtil.generateAccessToken(
                    userDetails.getId(),
                    userDetails.getEmail(),
                    userDetails.getRoleWithoutPrefix()
            );

            String refreshToken = jwtAuthUtil.generateRefreshToken(
                    userDetails.getId(),
                    userDetails.getEmail()
            );

            // Extract token ID for session tracking
            String tokenId = jwtAuthUtil.getJtiFromToken(accessToken);

            // Create user session
            sessionManagementService.createUserSession(
                    userDetails.getId(),
                    tokenId,
                    ipAddress,
                    userAgent
            );

            // Set both tokens in HttpOnly cookies
            cookieUtil.addAccessTokenCookie(response, accessToken);
            cookieUtil.addRefreshTokenCookie(response, refreshToken);
            // Add tokens to response headers
            cookieUtil.addTokensToHeaders(response, accessToken, refreshToken);

            // Get User entity to get complete information including timestamps
            User user = userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate presigned URL for profile picture if it exists
        String profilePictureUrl = null;
        if (user.getProfilePictureUrl() != null) {
            try {
                profilePictureUrl = preSignedUrlService.generatePresignedUrl(user.getProfilePictureUrl());
            } catch (Exception e) {
                log.warn("Failed to generate pre-signed URL for user profile picture during login: {}", e.getMessage());
            }
        }

        // Get current session count after login
        int finalSessionCount = sessionManagementService.getActiveSessionCount(userDetails.getId());

        LoginResponseDto loginResponse = LoginResponseDto.builder()
                .userId(user.getId().toString())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .username(user.getUsername())
                .phoneNumber(user.getPhoneNumber())
                .profilePictureUrl(profilePictureUrl)
                .role(user.getRole().getRole().name())
                .requiresOnboarding(user.isNewUser())
                .build();

        log.info("Login successful for user: {} (Active sessions: {}/{})",
                userDetails.getEmail(), finalSessionCount, maxConcurrentSessions);
        return loginResponse;

    }

    @Override
    public RefreshTokenResponseDto refreshToken(HttpServletRequest request, HttpServletResponse response) {
        log.info("Token refresh attempt");

            // Get refresh token from HttpOnly cookie
            String refreshTokenFromCookie = cookieUtil.getRefreshTokenFromCookie(request);

            if (refreshTokenFromCookie == null) {
                log.warn("Token refresh failed: No refresh token found in cookie");
                throw new RuntimeException("Refresh token not found");
            }

            // Validate refresh token
            JwtAuthUtil.AuthTokenData tokenData = jwtAuthUtil.validateAndParseRefreshToken(
                    refreshTokenFromCookie
            );

            // Verify user still exists and is active
            Optional<User> userOptional = userRepository.findById(tokenData.getUserId());
            if (userOptional.isEmpty()) {
                log.warn("Token refresh failed: User not found for ID: {}", tokenData.getUserId());
                throw new RuntimeException("User not found");
            }

            User user = userOptional.get();
            if (user.getStatus() != EStatus.ACTIVE) {
                log.warn("Token refresh failed: User not active: {}", user.getEmail());
                throw new RuntimeException("User account is not active");
            }

            // Check if user still has active session
            if (!sessionManagementService.hasActiveSession(user.getId())) {
                log.warn("Token refresh failed: No active session for user: {}", user.getEmail());
                throw new RuntimeException("No active session found");
            }

            // Generate new access token
            String newAccessToken = jwtAuthUtil.generateAccessToken(
                    user.getId(),
                    user.getEmail(),
                    user.getRole().getRole().name()
            );

            // Update session activity
            sessionManagementService.updateSessionActivity(user.getId());

            // Set new access token in cookie (refresh token remains the same)
            cookieUtil.addAccessTokenCookie(response, newAccessToken);
            // Add access token to response header
            cookieUtil.addAccessTokenToHeader(response, newAccessToken);

            RefreshTokenResponseDto refreshResponse = RefreshTokenResponseDto.builder()
                    .message("Access token refreshed successfully")
                    .build();
            log.info("Token refreshed successfully for user: {}", user.getEmail());
            return refreshResponse;

    }

    /**
     * Logout user from current session only (other devices remain logged in)
     */
    @Override
    public LogoutResponseDto logout(HttpServletResponse response, HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            log.info("User logout initiated: {}", userDetails.getEmail());

            // Extract and blacklist access token from cookie
            String accessToken = cookieUtil.getAccessTokenFromCookie(request);
            if (accessToken != null) {
                try {
                    String jti = jwtAuthUtil.getJtiFromToken(accessToken);
                    java.util.Date expiration = jwtAuthUtil.getExpirationDateFromToken(accessToken);

                    if (jti != null && expiration != null) {
                        // Blacklist the access token
                        tokenBlacklistService.blacklistToken(jti, expiration.getTime());

                        // Remove only this specific session (not all sessions)
                        sessionManagementService.removeUserSessionByToken(userDetails.getId(), jti);

                        int remainingSessions = sessionManagementService.getActiveSessionCount(userDetails.getId());
                        log.info("Access token blacklisted and session removed for user: {} (Remaining sessions: {})",
                                userDetails.getEmail(), remainingSessions);
                    } else {
                        log.warn("Could not extract JTI or expiration from access token during logout");
                        // Fallback: remove all sessions if token parsing fails
                        sessionManagementService.removeUserSession(userDetails.getId());
                    }

                } catch (Exception e) {
                    log.warn("Failed to blacklist access token during logout: {}", e.getMessage());
                    // Fallback: remove all sessions if token processing fails
                    sessionManagementService.removeUserSession(userDetails.getId());
                }
            } else {
                log.debug("No access token found in cookie during logout");
                // Fallback: remove all sessions
                sessionManagementService.removeUserSession(userDetails.getId());
            }
        }

        // Clear all authentication cookies
        cookieUtil.clearAllAuthCookies(response);

        // Clear security context
        SecurityContextHolder.clearContext();

        return LogoutResponseDto.builder()
                .message("Successfully logged out")
                .logoutTime(Instant.now())
                .build();
    }
    /**
     * Get current authenticated user information
     */
    @Override
    public UserProfileDto getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new RuntimeException("No authenticated user found");
        }

        // Get User entity for complete information
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfileDto userProfileDto = userMapper.toUserProfileDto(user);
        if (user.getProfilePictureUrl() != null) {
            try {
                String presignedUrl = preSignedUrlService.generatePresignedUrl(user.getProfilePictureUrl());
                userProfileDto.setProfilePictureUrl(presignedUrl);
            } catch (Exception e) {
                log.warn("Failed to generate pre-signed URL for user profile picture: {}", e.getMessage());
                userProfileDto.setProfilePictureUrl(null);
            }
        }

        return userProfileDto;
    }

    @Override
    @Transactional
    public ApiResponseDto<String> completeOnboarding() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new RuntimeException("No authenticated user found");
        }

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getOnBoardAt() != null) {
            return ApiResponseDto.success("User has already completed onboarding");
        }

        user.setOnBoardAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("User {} completed onboarding", user.getEmail());
        return ApiResponseDto.success("Onboarding completed successfully");
    }

    /**
     * Extract client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
