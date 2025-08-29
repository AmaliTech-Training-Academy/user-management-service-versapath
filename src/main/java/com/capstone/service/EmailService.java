package com.capstone.service;

import com.capstone.model.ERole;

public interface EmailService {
    void sendRegistrationInvitation(String toEmail, String registrationLink, ERole roleName);
    void sendPasswordResetEmail(String toEmail, String resetLink, String userName);
}
