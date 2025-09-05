package com.capstone.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionManagementService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String USER_SESSIONS_PREFIX = "user_sessions:";
    private static final Duration SESSION_TIMEOUT = Duration.ofHours(24); // 24 hours

    @Value("${APP_SESSION_MAX_CONCURRENT_SESSIONS:3}")
    private int maxConcurrentSessions;

    /**
     * Check if user has an active session
     */
    public boolean hasActiveSession(UUID userId) {
        List<SessionInfo> sessions = getUserSessions(userId);
        boolean hasActive = !sessions.isEmpty();

        log.debug("User {} has {} active session(s)", userId, sessions.size());
        return hasActive;
    }
    /**
     * Create a new session for user with same device replacement
     */
    public void createUserSession(UUID userId, String tokenId, String ipAddress, String userAgent) {
        String key = USER_SESSIONS_PREFIX + userId.toString();

        // Get existing sessions
        List<SessionInfo> existingSessions = getUserSessions(userId);

        // Remove existing session from same device (IP + UserAgent)
        boolean sameDeviceReplaced = existingSessions.removeIf(session ->
                session.getIpAddress().equals(ipAddress) &&
                        Objects.equals(session.getUserAgent(), userAgent)
        );

        if (sameDeviceReplaced) {
            log.info("Replaced existing session for user {} on same device", userId);
        }

        // Create new session
        SessionInfo newSession = SessionInfo.builder()
                .userId(userId.toString())
                .tokenId(tokenId)
                .loginTime(LocalDateTime.now())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .deviceId(generateDeviceId(ipAddress, userAgent))
                .build();

        // Add new session to list
        existingSessions.add(newSession);

        // Enforce session limit - remove oldest sessions if exceeded
        if (existingSessions.size() > maxConcurrentSessions) {
            log.info("User {} exceeded max sessions ({}). Removing oldest sessions.",
                    userId, maxConcurrentSessions);

            // Sort by login time and keep only the most recent sessions
            existingSessions = existingSessions.stream()
                    .sorted(Comparator.comparing(SessionInfo::getLoginTime).reversed())
                    .limit(maxConcurrentSessions)
                    .collect(Collectors.toList());
        }

        // Store updated sessions
        try {
            String sessionsData = objectMapper.writeValueAsString(existingSessions);
            redisTemplate.opsForValue().set(key, sessionsData, SESSION_TIMEOUT);

            log.info("Session created for user: {} on device: {} (Total active: {})",
                    userId, newSession.getDeviceId(), existingSessions.size());
        } catch (JsonProcessingException e) {
            log.error("Failed to create session for user: {}", userId, e);
            throw new RuntimeException("Failed to create user session", e);
        }
    }

    /**
     * Remove user session (for logout) - Remove all sessions for backward compatibility
     */
    public void removeUserSession(UUID userId) {
        removeAllUserSessions(userId);
    }

    /**
     * Remove specific session by token ID (for individual logout)
     */
    public void removeUserSessionByToken(UUID userId, String tokenId) {
        String key = USER_SESSIONS_PREFIX + userId.toString();

        List<SessionInfo> sessions = getUserSessions(userId);
        boolean removed = sessions.removeIf(session -> session.getTokenId().equals(tokenId));

        if (removed) {
            try {
                if (sessions.isEmpty()) {
                    // No sessions left, remove the key
                    redisTemplate.delete(key);
                    log.info("All sessions removed for User: {}", userId);
                } else {
                    // Update with remaining sessions
                    String sessionsData = objectMapper.writeValueAsString(sessions);
                    redisTemplate.opsForValue().set(key, sessionsData, SESSION_TIMEOUT);
                    log.info("Session with token {} removed for user: {} (Remaining: {})",
                            tokenId, userId, sessions.size());
                }
            } catch (JsonProcessingException e) {
                log.error("Failed to update sessions after removal for user: {}", userId, e);
            }
        } else {
            log.debug("No session found with token {} for user: {}", tokenId, userId);
        }
    }


    /**
     * Remove all sessions for user
     */
    public void removeAllUserSessions(UUID userId) {
        String key = USER_SESSIONS_PREFIX + userId.toString();
        Boolean deleted = redisTemplate.delete(key);

        if (deleted) {
            log.info("All sessions removed for user: {}", userId);
        } else {
            log.debug("No sessions found to remove for user: {}", userId);
        }
    }


    /**
     * Get all sessions for user
     */
    public List<SessionInfo> getUserSessions(UUID userId) {
        String key = USER_SESSIONS_PREFIX + userId.toString();
        String sessionsData = (String) redisTemplate.opsForValue().get(key);

        if (sessionsData != null) {
            try {
                TypeReference<List<SessionInfo>> typeRef = new TypeReference<>() {
                };
                return objectMapper.readValue(sessionsData, typeRef);
            } catch (JsonProcessingException e) {
                log.warn("Failed to parse sessions data for user: {}. Cleaning up corrupted data.", userId);
                redisTemplate.delete(key);
                return new ArrayList<>();
            }
        }

        return new ArrayList<>();
    }

    /**
     * Update session activity (extend session timeout)
     */
    public void updateSessionActivity(UUID userId) {
        String key = USER_SESSIONS_PREFIX + userId.toString();

        // Check if session exists
        if (redisTemplate.hasKey(key)) {
            // Extend session timeout
            redisTemplate.expire(key, SESSION_TIMEOUT);
            log.debug("Session activity updated for user: {}", userId);
        }
    }


    /**
     * Get active session count for user
     */
    public int getActiveSessionCount(UUID userId) {
        return getUserSessions(userId).size();
    }

    /**
     * Generate device ID from IP and UserAgent
     */
    private String generateDeviceId(String ipAddress, String userAgent) {
        String combined = ipAddress + ":" + (userAgent != null ? userAgent : "unknown");
        return "device_" + Math.abs(combined.hashCode());
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
        private String deviceId;
    }
}