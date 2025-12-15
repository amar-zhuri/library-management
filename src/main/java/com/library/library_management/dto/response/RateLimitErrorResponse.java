package com.library.library_management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateLimitErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private int retryAfterSeconds;
    private String path;

    public static RateLimitErrorResponse of(String path, int retryAfterSeconds) {
        return RateLimitErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(429)
                .error("Too Many Requests")
                .message("Rate limit exceeded. Please try again in " + retryAfterSeconds + " seconds.")
                .retryAfterSeconds(retryAfterSeconds)
                .path(path)
                .build();
    }
}