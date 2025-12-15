package com.library.library_management.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasks {

    private final RateLimitFilter rateLimitFilter;

    /**
     * Clean up rate limit cache every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void cleanupRateLimitCache() {
        rateLimitFilter.cleanup();
    }
}