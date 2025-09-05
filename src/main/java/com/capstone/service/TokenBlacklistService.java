package com.capstone.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String BLACKLIST_PREFIX = "blacklisted_token:";
    
    /**
     * Add token to blacklist with TTL based on token expiration
     * @param jti JWT Token ID
     * @param expirationTimeMillis Token expiration time in milliseconds
     */
    public void blacklistToken(String jti, long expirationTimeMillis) {
        String key = BLACKLIST_PREFIX + jti;
        long ttlSeconds = (expirationTimeMillis - System.currentTimeMillis()) / 1000;
        
        if (ttlSeconds > 0) {
            redisTemplate.opsForValue().set(key, "blacklisted", Duration.ofSeconds(ttlSeconds));
            log.info("Token blacklisted: {} with TTL: {} seconds", jti, ttlSeconds);
        } else {
            log.warn("Attempted to blacklist expired token: {}", jti);
        }
    }
    
}