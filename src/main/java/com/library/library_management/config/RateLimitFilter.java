package com.library.library_management.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.library_management.dto.response.RateLimitErrorResponse;
import com.library.library_management.security.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Component
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RateLimitFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    // Store request timestamps per client
    // Key: "user:123" or "ip:192.168.1.1"
    // Value: List of request timestamps (epoch seconds)
    private final Map<String, List<Long>> requestCounts = new ConcurrentHashMap<>();

    // Rate limits (requests per minute)
    private static final int LIMIT_ANONYMOUS = 30;
    private static final int LIMIT_AUTHENTICATED = 100;
    private static final int LIMIT_ADMIN = 300;

    // Time window in seconds
    private static final int WINDOW_SECONDS = 60;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Skip rate limiting for certain paths (optional)
        String path = request.getRequestURI();
        if (shouldSkip(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Determine client identifier and rate limit
        ClientInfo clientInfo = getClientInfo(request);
        String clientKey = clientInfo.key;
        int limit = clientInfo.limit;

        // Get current timestamp
        long now = Instant.now().getEpochSecond();

        // Get or create request list for this client
        List<Long> timestamps = requestCounts.computeIfAbsent(clientKey, k -> new CopyOnWriteArrayList<>());

        // Remove old timestamps (outside the time window)
        long windowStart = now - WINDOW_SECONDS;
        List<Long> recentRequests = timestamps.stream()
                .filter(ts -> ts > windowStart)
                .collect(Collectors.toList());

        // Update the stored list
        timestamps.clear();
        timestamps.addAll(recentRequests);

        // Check if over limit
        int currentCount = recentRequests.size();

        if (currentCount >= limit) {
            // Rate limited!
            log.warn("Rate limit exceeded for client: {} (count: {}, limit: {})", clientKey, currentCount, limit);

            // Calculate retry after
            long oldestRequest = recentRequests.isEmpty() ? now : recentRequests.get(0);
            int retryAfter = (int) ((oldestRequest + WINDOW_SECONDS) - now);
            if (retryAfter < 1) retryAfter = 1;

            // Send 429 response
            sendRateLimitResponse(response, request.getRequestURI(), limit, 0, retryAfter);
            return;
        }

        // Add current request timestamp
        timestamps.add(now);

        // Calculate remaining requests
        int remaining = limit - currentCount - 1;
        long resetTime = now + WINDOW_SECONDS;

        // Add rate limit headers to response
        response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, remaining)));
        response.setHeader("X-RateLimit-Reset", String.valueOf(resetTime));

        // Continue with the request
        filterChain.doFilter(request, response);
    }

    /**
     * Get client identifier and applicable rate limit
     */
    private ClientInfo getClientInfo(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                
                if (jwtService.validateToken(token)) {
                    Long userId = jwtService.extractUserId(token);
                    String role = jwtService.extractRole(token);

                    if ("ADMIN".equals(role)) {
                        return new ClientInfo("admin:" + userId, LIMIT_ADMIN);
                    } else {
                        return new ClientInfo("user:" + userId, LIMIT_AUTHENTICATED);
                    }
                }
            } catch (Exception e) {
                log.debug("Could not extract user from token: {}", e.getMessage());
            }
        }

        // Anonymous - use IP address
        String clientIp = getClientIp(request);
        return new ClientInfo("ip:" + clientIp, LIMIT_ANONYMOUS);
    }

    /**
     * Get client IP address
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * Check if path should skip rate limiting
     */
    private boolean shouldSkip(String path) {
        // Skip health check
        return path.equals("/api/health");
    }

    /**
     * Send 429 Too Many Requests response
     */
    private void sendRateLimitResponse(HttpServletResponse response, String path,
                                        int limit, int remaining, int retryAfter) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
        response.setHeader("Retry-After", String.valueOf(retryAfter));

        RateLimitErrorResponse errorResponse = RateLimitErrorResponse.of(path, retryAfter);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    /**
     * Simple holder for client info
     */
    private record ClientInfo(String key, int limit) {}

    /**
     * Cleanup old entries periodically (called manually or via scheduled task)
     */
    public void cleanup() {
        long windowStart = Instant.now().getEpochSecond() - WINDOW_SECONDS;

        requestCounts.forEach((key, timestamps) -> {
            timestamps.removeIf(ts -> ts <= windowStart);
        });

        // Remove empty entries
        requestCounts.entrySet().removeIf(entry -> entry.getValue().isEmpty());

        log.debug("Rate limit cache cleanup complete. Active clients: {}", requestCounts.size());
    }
}
