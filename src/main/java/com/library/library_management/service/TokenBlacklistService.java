package com.library.library_management.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class TokenBlacklistService {

    // Store blacklisted tokens with their expiry time
    // Key: token, Value: expiry timestamp
    private final Map<String, Instant> blacklistedTokens = new ConcurrentHashMap<>();

    /**
     * Add token to blacklist
     */
    public void blacklistToken(String token, Instant expiryTime) {
        blacklistedTokens.put(token, expiryTime);
        log.debug("Token blacklisted, expires at: {}", expiryTime);
    }

    /**
     * Check if token is blacklisted
     */
    public boolean isBlacklisted(String token) {
        return blacklistedTokens.containsKey(token);
    }

    /**
     * Cleanup expired tokens every hour
     */
    @Scheduled(fixedRate = 3600000)
    public void cleanupExpiredTokens() {
        Instant now = Instant.now();
        int before = blacklistedTokens.size();

        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue().isBefore(now));

        int removed = before - blacklistedTokens.size();
        if (removed > 0) {
            log.debug("Cleaned up {} expired blacklisted tokens", removed);
        }
    }
}
