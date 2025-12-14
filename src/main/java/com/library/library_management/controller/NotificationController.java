package com.library.library_management.controller;

import com.library.library_management.dto.request.NotificationPreferencesRequest;
import com.library.library_management.dto.response.MessageResponse;
import com.library.library_management.dto.response.NotificationPreferencesResponse;
import com.library.library_management.security.CustomUserDetails;
import com.library.library_management.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Get user's notification preferences
     * GET /api/notifications/preferences
     */
    @GetMapping("/preferences")
    public ResponseEntity<NotificationPreferencesResponse> getPreferences(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("GET /api/notifications/preferences for user {}", userDetails.getId());
        NotificationPreferencesResponse response = notificationService.getPreferences(userDetails.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Update user's notification preferences
     * PUT /api/notifications/preferences
     */
    @PutMapping("/preferences")
    public ResponseEntity<NotificationPreferencesResponse> updatePreferences(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody NotificationPreferencesRequest request) {
        log.info("PUT /api/notifications/preferences for user {}", userDetails.getId());
        NotificationPreferencesResponse response = notificationService.updatePreferences(userDetails.getId(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * Unsubscribe from all notifications (public - no auth required)
     * GET /api/notifications/unsubscribe?token=xxx
     */
    @GetMapping("/unsubscribe")
    public ResponseEntity<MessageResponse> unsubscribeAll(@RequestParam String token) {
        log.info("GET /api/notifications/unsubscribe");
        notificationService.unsubscribeAll(token);
        return ResponseEntity.ok(MessageResponse.success("You have been unsubscribed from all notifications."));
    }

    /**
     * Unsubscribe from newsletter only (public - no auth required)
     * GET /api/notifications/unsubscribe/newsletter?token=xxx
     */
    @GetMapping("/unsubscribe/newsletter")
    public ResponseEntity<MessageResponse> unsubscribeNewsletter(@RequestParam String token) {
        log.info("GET /api/notifications/unsubscribe/newsletter");
        notificationService.unsubscribeNewsletter(token);
        return ResponseEntity.ok(MessageResponse.success("You have been unsubscribed from the newsletter."));
    }
}