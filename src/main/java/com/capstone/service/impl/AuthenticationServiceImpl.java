package com.capstone.service.impl;

import com.capstone.model.EStatus;
import com.capstone.model.User;
import com.capstone.service.AuthenticationService;
import com.capstone.repository.UserRepository;
import com.capstone.security.CustomUserDetails;
import com.capstone.util.CookieUtil;
import com.capstone.util.JwtAuthUtil;
import com.capstone.dto.request.LoginRequestDto;
import com.capstone.dto.response.LoginResponseDto;
import com.capstone.dto.response.LogoutResponseDto;
import com.capstone.dto.response.RefreshTokenResponseDto;
import com.capstone.dto.response.UserInfoDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
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

    /**
     * Authenticate user and generate JWT tokens
     */
    @Override
    public LoginResponseDto login(LoginRequestDto loginRequest, HttpServletResponse response) {
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

            // Set refresh token in HttpOnly cookie
            cookieUtil.addRefreshTokenCookie(response, refreshToken);

            // Build response
            UserInfoDto userInfo = UserInfoDto.builder()
                    .id(userDetails.getId().toString())
                    .email(userDetails.getEmail())
                    .username(userDetails.getUsername())
                    .firstName(userDetails.getFirstName())
                    .lastName(userDetails.getLastName())
                    .role(userDetails.getRoleWithoutPrefix())
                    .status(userDetails.getStatus().name())
                    .build();

            LoginResponseDto response1 = LoginResponseDto.builder()
                    .userInfo(userInfo)
                    .tokenType("Bearer")
                    .accessToken(accessToken)
                    .expiresIn(900000) // 15 minutes
                    .build();

            log.info("Login successful for user: {}", userDetails.getEmail());
            return response1;

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

            // Generate new access token
            String newAccessToken = jwtAuthUtil.generateAccessToken(
                    user.getId(),
                    user.getEmail(),
                    user.getRole().getRole().name()
            );

            RefreshTokenResponseDto refreshResponse = RefreshTokenResponseDto.builder()
                    .accessToken(newAccessToken)
                    .tokenType("Bearer")
                    .expiresIn(900000) // 15 minutes
                    .build();

            log.info("Token refreshed successfully for user: {}", user.getEmail());
            return refreshResponse;

    }

    /**
     * Logout user (clear security context)
     */
    @Override
    public LogoutResponseDto logout(HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            log.info("User logged out: {}", userDetails.getEmail());
        }

        // Clear refresh token cookie
        cookieUtil.clearRefreshTokenCookie(response);

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
    public UserInfoDto getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new RuntimeException("No authenticated user found");
        }

        return UserInfoDto.builder()
                .id(userDetails.getId().toString())
                .email(userDetails.getEmail())
                .username(userDetails.getUsername())
                .firstName(userDetails.getFirstName())
                .lastName(userDetails.getLastName())
                .role(userDetails.getRoleWithoutPrefix())
                .status(userDetails.getStatus().name())
                .build();
    }
}
