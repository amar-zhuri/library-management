package com.library.library_management.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreferences {

    @Id
    private Long id; // Same as user ID (one-to-one)

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    // Email notification settings
    @Column(name = "newsletter_enabled", nullable = false)
    @Builder.Default
    private Boolean newsletterEnabled = true;

    @Column(name = "new_books_enabled", nullable = false)
    @Builder.Default
    private Boolean newBooksEnabled = true;

    @Column(name = "weekly_digest_enabled", nullable = false)
    @Builder.Default
    private Boolean weeklyDigestEnabled = false;

    @Column(name = "reading_reminders_enabled", nullable = false)
    @Builder.Default
    private Boolean readingRemindersEnabled = false;

    @Column(name = "achievement_notifications_enabled", nullable = false)
    @Builder.Default
    private Boolean achievementNotificationsEnabled = true;

    // Unsubscribe token (for one-click unsubscribe)
    @Column(name = "unsubscribe_token", unique = true)
    private String unsubscribeToken;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Check if user wants any emails at all
     */
    public boolean isAnyEmailEnabled() {
        return newsletterEnabled || newBooksEnabled || weeklyDigestEnabled || 
               readingRemindersEnabled || achievementNotificationsEnabled;
    }
}