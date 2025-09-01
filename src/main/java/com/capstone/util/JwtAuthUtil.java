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
public class JwtAuthUtil {

    @Value("${JWT_SECRET}")
    private String jwtSecret;

    @Value("${JWT_ACCESS_TOKEN_EXPIRATION_MS:900000}") // 15 minutes
    private long accessTokenExpirationMs;

    @Value("${JWT_REFRESH_TOKEN_EXPIRATION_MS:604800000}") // 7 days in milliseconds
    private long refreshTokenExpirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Generate access token for authenticated user
     */
    public String generateAccessToken(UUID userId, String email, String role) {
        Instant now = Instant.now();
        String jti = UUID.randomUUID().toString(); // Add unique token ID

        return Jwts.builder()
                .subject(userId.toString())
                .id(jti) // Add JWT ID for blacklisting
                .claim("email", email)
                .claim("role", role)
                .claim("type", "ACCESS")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(accessTokenExpirationMs)))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Generate refresh token for token renewal
     */
    public String generateRefreshToken(UUID userId, String email) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("type", "REFRESH")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(refreshTokenExpirationMs)))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Validate and parse access token
     */
    public AuthTokenData validateAndParseAccessToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Verify token type
            String tokenType = claims.get("type", String.class);
            if (!"ACCESS".equals(tokenType)) {
                throw new RuntimeException("Invalid token type. Expected ACCESS token");
            }

            // Check expiration
            validateTokenExpiration(claims);

            return AuthTokenData.builder()
                    .userId(UUID.fromString(claims.getSubject()))
                    .email(claims.get("email", String.class))
                    .role(claims.get("role", String.class))
                    .tokenType(tokenType)
                    .issuedAt(claims.getIssuedAt().toInstant())
                    .expiresAt(claims.getExpiration().toInstant())
                    .build();

        } catch (ExpiredJwtException e) {
            log.error("Access token has expired");
            throw new RuntimeException("Access token has expired", e);
        } catch (MalformedJwtException e) {
            log.error("Invalid access token format");
            throw new RuntimeException("Invalid access token format", e);
        } catch (Exception e) {
            log.error("Invalid access token: {}", e.getMessage());
            throw new RuntimeException("Invalid access token", e);
        }
    }

    /**
     * Validate and parse refresh token
     */
    public AuthTokenData validateAndParseRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Verify token type
            String tokenType = claims.get("type", String.class);
            if (!"REFRESH".equals(tokenType)) {
                throw new RuntimeException("Invalid token type. Expected REFRESH token");
            }

            // Check expiration
            validateTokenExpiration(claims);

            return AuthTokenData.builder()
                    .userId(UUID.fromString(claims.getSubject()))
                    .email(claims.get("email", String.class))
                    .tokenType(tokenType)
                    .issuedAt(claims.getIssuedAt().toInstant())
                    .expiresAt(claims.getExpiration().toInstant())
                    .build();

        } catch (ExpiredJwtException e) {
            log.error("Refresh token has expired");
            throw new RuntimeException("Refresh token has expired", e);
        } catch (Exception e) {
            log.error("Invalid refresh token: {}", e.getMessage());
            throw new RuntimeException("Invalid refresh token", e);
        }
    }

    /**
     * Extract JWT ID (JTI) from token for blacklisting
     */
    public String getJtiFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getId();
        } catch (Exception e) {
            log.warn("Could not extract JTI from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extract expiration date from token for blacklist TTL calculation
     */
    public Date getExpirationDateFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getExpiration();
        } catch (Exception e) {
            log.warn("Could not extract expiration from token: {}", e.getMessage());
            return null;
        }
    }

    private void validateTokenExpiration(Claims claims) {
        Instant expiration = claims.getExpiration().toInstant();
        Instant now = Instant.now();
        if (expiration.isBefore(now)) {
            throw new ExpiredJwtException(null, claims, "Token has expired");
        }
    }

    /*
     * Data class to hold parsed authentication token information
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AuthTokenData {
        private UUID userId;
        private String email;
        private String role;
        private String tokenType;
        private Instant issuedAt;
        private Instant expiresAt;
    }
}
