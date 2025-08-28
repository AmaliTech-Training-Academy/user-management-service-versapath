package com.capstone.service;

import com.capstone.dto.request.ForgotPasswordRequest;
import com.capstone.dto.request.ResetPasswordRequest;
import com.capstone.dto.response.PasswordResetResponse;

public interface PasswordResetService {
    
    /**
     * Process forgot password request - sends reset email if user exists
     */
    PasswordResetResponse processForgotPassword(ForgotPasswordRequest request);
    
    /**
     * Reset user password using valid token
     */
    PasswordResetResponse resetPassword(String token, ResetPasswordRequest request);
}