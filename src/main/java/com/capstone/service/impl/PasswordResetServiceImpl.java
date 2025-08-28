package com.capstone.service.impl;

import com.capstone.dto.request.ForgotPasswordRequest;
import com.capstone.dto.request.ResetPasswordRequest;
import com.capstone.dto.response.PasswordResetResponse;
import com.capstone.exception.InvalidPasswordResetTokenException;
import com.capstone.exception.PasswordMismatchException;
import com.capstone.exception.TokenAlreadyUsedException;
import com.capstone.exception.UserNotActiveForPasswordResetException;
import com.capstone.model.EStatus;
import com.capstone.model.User;
import com.capstone.repository.UserRepository;
import com.capstone.service.EmailService;
import com.capstone.service.PasswordResetService;
import com.capstone.util.PasswordResetTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordResetTokenUtil passwordResetTokenUtil;
    private final PasswordEncoder passwordEncoder;

    @Value("${APP_PASSWORD_RESET_FE_URI}")
    private String passwordResetFrontendUri;


    @Override
    public PasswordResetResponse processForgotPassword(ForgotPasswordRequest request) {
        // Find active user by email
        User user = userRepository.findByEmailAndStatus(request.getEmail(), EStatus.ACTIVE)
                .orElseThrow(() -> new UserNotActiveForPasswordResetException("User not found or not active"));

        if (user != null) {
            // Generate password signature for one-time use
            String passwordSignature = generatePasswordSignature(user.getPassword());

            // Generate reset token
            String resetToken = passwordResetTokenUtil.generatePasswordResetToken(
                    user.getId(), 
                    user.getEmail(), 
                    passwordSignature
            );

            // Create reset link
            String resetLink = passwordResetFrontendUri + resetToken;

            // Send password reset email
            emailService.sendPasswordResetEmail(user.getEmail(), resetLink, user.getRole().getRole());

            log.info("Password reset email sent to: {}", user.getEmail());
            return new PasswordResetResponse("A password reset link has been sent.");
        } else {
            log.warn("Password reset requested for non-existent or inactive user: {}", request.getEmail());

            return new PasswordResetResponse("A password reset link has been not sent.\n User not found or not active");
        }
    }

    @Override
    @Transactional
    public PasswordResetResponse resetPassword(String token, ResetPasswordRequest request) {
        // Validate passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException("New password and confirm password do not match");
        }

        // Parse and validate token
        PasswordResetTokenUtil.PasswordResetTokenData tokenData = 
                passwordResetTokenUtil.validateAndParseToken(token);

        // Find user
        User user = userRepository.findByEmailAndStatus(tokenData.getEmail(), EStatus.ACTIVE)
                .orElseThrow(() -> new UserNotActiveForPasswordResetException("User not found or not active"));

        // Verify user ID matches token
        if (!user.getId().equals(tokenData.getUserId())) {
            throw new InvalidPasswordResetTokenException("Invalid token");
        }

        // Verify password signature (one-time use check)
        String currentPasswordSignature = generatePasswordSignature(user.getPassword());
        if (!currentPasswordSignature.equals(tokenData.getPasswordSignature())) {
            throw new TokenAlreadyUsedException("Password reset token has already been used");
        }

        // Update user password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("Password successfully reset for user: {}", user.getEmail());
        return new PasswordResetResponse("Password has been reset successfully");
    }


    /**
     * Generate password signature for one-time token validation
     */
    private String generatePasswordSignature(String passwordHash) {
        // Use first 20 characters of BCrypt hash as signature
        return passwordHash.length() >= 20 ? passwordHash.substring(0, 20) : passwordHash;
    }
}