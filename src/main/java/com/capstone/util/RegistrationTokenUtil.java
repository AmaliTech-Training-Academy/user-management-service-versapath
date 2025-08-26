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
public class RegistrationTokenUtil {

    @Value("${APP_REGISTRATION_TOKEN_SECRET}")
    private String registrationTokenSecret;

    @Value("${APP_REGISTRATION_TOKEN_EXPIRATION_MS:86400000}")
    private long expirationsMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(registrationTokenSecret.getBytes());
    }

    /**
     * Generate a registration token containing user ID, email, and role ID
     */
    public String generateRegistrationToken(UUID userId, String email, UUID roleId) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("roleId", roleId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationsMs)))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Validate and parse registration token
     */
    public RegistrationTokenData validateAndParseToken(String token) {
        try{
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Check if token is expired
            Instant expiration = claims.getExpiration().toInstant();
            Instant now = Instant.now();
            if(expiration.isBefore(now)){
                throw new ExpiredJwtException(null, claims, "Registration token has expired");
            }

            return RegistrationTokenData.builder()
                    .userId(UUID.fromString(claims.getSubject()))
                    .email(claims.get("email", String.class))
                    .roleId(UUID.fromString(claims.get("roleId", String.class)))
                    .issuedAt(claims.getIssuedAt().toInstant())
                    .expiresAt(expiration)
                    .build();
        } catch (ExpiredJwtException e) {
            log.error("Registration token has expired for token: {}", token.substring(0, Math.min(token.length(), 20)) + "...");
            throw new RuntimeException("Registration token has expired", e);
        } catch (MalformedJwtException e){
            log.error("Invalid registration token format for token: {}", token.substring(0, Math.min(token.length(), 20)) + "...");
            throw new RuntimeException("Invalid registration token format", e);
        } catch(Exception e){
            log.error("Invalid registration token: {}", e.getMessage());
            throw new RuntimeException("Invalid registration token", e);
        }
    }

    /**
     * Data class to hold parsed token information
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RegistrationTokenData {
        private UUID userId;
        private String email;
        private UUID roleId;
        private Instant issuedAt;
        private Instant expiresAt;
    }
}