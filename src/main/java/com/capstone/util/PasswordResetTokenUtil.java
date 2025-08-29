package com.capstone.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Component
@Slf4j
public class PasswordResetTokenUtil {

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generate a random raw token for password reset
     */
    public String generateRawToken() {
        byte[] randomBytes = new byte[32]; // 256 bits
        secureRandom.nextBytes(randomBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        
        log.debug("Generated raw token of length: {}", rawToken.length());
        return rawToken;
    }

    /**
     * Hash raw token using SHA-256 for database storage
     */
    public String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            String hashedToken = Base64.getUrlEncoder().withoutPadding().encodeToString(hashBytes);
            
            log.debug("Hashed token using SHA-256");
            return hashedToken;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Verify raw token against SHA-256 hash stored in database
     */
    public boolean verifyToken(String rawToken, String hashedToken) {
        String computedHash = hashToken(rawToken);
        boolean matches = computedHash.equals(hashedToken);
        log.debug("SHA-256 token verification result: {}", matches);
        return matches;
    }
}