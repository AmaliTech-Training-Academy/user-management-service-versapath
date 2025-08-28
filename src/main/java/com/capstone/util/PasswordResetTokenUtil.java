package com.capstone.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
@Slf4j
public class PasswordResetTokenUtil {

    @Value("${APP_PASSWORD_RESET_TOKEN_SECRET}")
    private String passwordResetTokenSecret;

    @Value("${APP_PASSWORD_RESET_TOKEN_EXPIRATION_MS:3600000}")
    private long expirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(passwordResetTokenSecret.getBytes());
    }

    /**
     * Generate a password reset token containing user ID, email, and password signature
     */
    public String generatePasswordResetToken(UUID userId, String email, String passwordSignature) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("type", "password_reset")
                .claim("pwdSig", passwordSignature)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Validate and parse password reset token
     */
    public PasswordResetTokenData validateAndParseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Verify token type
            String tokenType = claims.get("type", String.class);
            if (!"password_reset".equals(tokenType)) {
                throw new MalformedJwtException("Invalid token type");
            }

            // Check if token is expired
            Instant expiration = claims.getExpiration().toInstant();
            Instant now = Instant.now();
            if (expiration.isBefore(now)) {
                throw new ExpiredJwtException(null, claims, "Password reset token has expired");
            }

            return PasswordResetTokenData.builder()
                    .userId(UUID.fromString(claims.getSubject()))
                    .email(claims.get("email", String.class))
                    .passwordSignature(claims.get("pwdSig", String.class))
                    .issuedAt(claims.getIssuedAt().toInstant())
                    .expiresAt(expiration)
                    .build();
        } catch (ExpiredJwtException e) {
            log.error("Password reset token has expired for token: {}", token.substring(0, Math.min(token.length(), 20)) + "...");
            throw new RuntimeException("Password reset token has expired", e);
        } catch (MalformedJwtException e) {
            log.error("Invalid password reset token format for token: {}", token.substring(0, Math.min(token.length(), 20)) + "...");
            throw new RuntimeException("Invalid password reset token format", e);
        } catch (Exception e) {
            log.error("Invalid password reset token: {}", e.getMessage());
            throw new RuntimeException("Invalid password reset token", e);
        }
    }

    /**
     * Data class to hold parsed token information
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PasswordResetTokenData {
        private UUID userId;
        private String email;
        private String passwordSignature;
        private Instant issuedAt;
        private Instant expiresAt;
    }
}