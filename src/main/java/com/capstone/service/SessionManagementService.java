package com.capstone.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionManagementService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String ACTIVE_SESSION_PREFIX = "active_session:";
    private static final Duration SESSION_TIMEOUT = Duration.ofHours(24); // 24 hours

    /**
     * Check if user has an active session
     */
    public boolean hasActiveSession(UUID userId) {
        String key = ACTIVE_SESSION_PREFIX + userId.toString();
        String sessionData = (String) redisTemplate.opsForValue().get(key);

        if (sessionData != null) {
            try {
                SessionInfo sessionInfo = objectMapper.readValue(sessionData, SessionInfo.class);
                log.debug("Active session found for user: {} with tokenId: {}", userId, sessionInfo.getTokenId());
                return true;
            } catch (JsonProcessingException e) {
                log.warn("Failed to parse session data for user: {}", userId);
                // Clean up corrupted session data
                removeUserSession(userId);
                return false;
            }
        }

        log.debug("No active session found for user: {}", userId);
        return false;
    }

    /**
     * Create a new session for user
     */
    public void createUserSession(UUID userId, String tokenId, String ipAddress, String userAgent) {
        String key = ACTIVE_SESSION_PREFIX + userId.toString();

        SessionInfo sessionInfo = SessionInfo.builder()
                .userId(userId.toString())
                .tokenId(tokenId)
                .loginTime(LocalDateTime.now())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        try {
            String sessionData = objectMapper.writeValueAsString(sessionInfo);
            redisTemplate.opsForValue().set(key, sessionData, SESSION_TIMEOUT);
            log.info("Session created for user: {} with tokenId: {}", userId, tokenId);
        } catch (JsonProcessingException e) {
            log.error("Failed to create session for user: {}", userId, e);
            throw new RuntimeException("Failed to create user session", e);
        }
    }

    /**
     * Remove user session (for logout)
     */
    public void removeUserSession(UUID userId) {
        String key = ACTIVE_SESSION_PREFIX + userId.toString();
        Boolean deleted = redisTemplate.delete(key);

        if (deleted) {
            log.info("Session removed for user: {}", userId);
        } else {
            log.debug("No session found to remove for user: {}", userId);
        }
    }

    /**
     * Get session information for user
     */
    public SessionInfo getUserSession(UUID userId) {
        String key = ACTIVE_SESSION_PREFIX + userId.toString();
        String sessionData = (String) redisTemplate.opsForValue().get(key);

        if (sessionData != null) {
            try {
                return objectMapper.readValue(sessionData, SessionInfo.class);
            } catch (JsonProcessingException e) {
                log.warn("Failed to parse session data for User: {}", userId);
                removeUserSession(userId);
                return null;
            }
        }

        return null;
    }

    /**
     * Update session activity (extend session timeout)
     */
    public void updateSessionActivity(UUID userId) {
        String key = ACTIVE_SESSION_PREFIX + userId.toString();

        // Check if session exists
        if (redisTemplate.hasKey(key)) {
            // Extend session timeout
            redisTemplate.expire(key, SESSION_TIMEOUT);
            log.debug("Session activity updated for user: {}", userId);
        }
    }


    /**
     * Session information data class
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SessionInfo {
        private String userId;
        private String tokenId;
        private LocalDateTime loginTime;
        private String ipAddress;
        private String userAgent;
    }
}