package com.capstone.service.impl;

import com.capstone.dto.request.ForgotPasswordRequest;
import com.capstone.dto.request.ResetPasswordRequest;
import com.capstone.dto.response.PasswordResetResponse;
import com.capstone.exception.InvalidPasswordResetTokenException;
import com.capstone.exception.PasswordMismatchException;
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
                .orElse(null);

        if (user != null) {
            // Generate raw reset token
            String rawToken = passwordResetTokenUtil.generateRawToken();

            // Hash token for database storage
            String hashedToken = passwordResetTokenUtil.hashToken(rawToken);

            // Set reset token and expiration (1 hour from now)
            user.setResetToken(hashedToken);
            user.setResetTokenExpiresAt(LocalDateTime.now().plusHours(1));
            userRepository.save(user);

            // Create reset link with raw token
            String resetLink = passwordResetFrontendUri + rawToken;

            // Send password reset email
            emailService.sendPasswordResetEmail(user.getEmail(), resetLink, user.getLastName());

            log.info("Password reset email sent to: {}", user.getEmail());
        } else {
            log.warn("Password reset requested for non-existent or inactive user: {}", request.getEmail());
        }

        // Always return same response for security (email enumeration protection)
        return new PasswordResetResponse("If the email exists and account is active, a password reset link has been sent.");
    }

    @Override
    @Transactional
    public PasswordResetResponse resetPassword(String token, ResetPasswordRequest request) {
        // Validate passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException("New password and confirm password do not match");
        }

        // Hash the received raw token
        String hashedToken = passwordResetTokenUtil.hashToken(token);

        // Find user with valid reset token (token exists and not expired)
        User user = userRepository.findByValidResetToken(hashedToken, LocalDateTime.now())
                .orElseThrow(() -> new InvalidPasswordResetTokenException("Invalid or expired password reset token"));

        // Verify token matches (additional security check)
        if (!passwordResetTokenUtil.verifyToken(token, user.getResetToken())) {
            throw new InvalidPasswordResetTokenException("Invalid password reset token");
        }

        // Update user password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        
        // Clear reset token fields (one-time use)
        user.setResetToken(null);
        user.setResetTokenExpiresAt(null);
        
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("Password successfully reset for user: {}", user.getEmail());
        return new PasswordResetResponse("Password has been reset successfully");
    }

}