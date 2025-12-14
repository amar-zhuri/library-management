package com.library.library_management.dto.response;

import com.library.library_management.entity.NotificationPreferences;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferencesResponse {

    private Boolean newsletterEnabled;
    private Boolean newBooksEnabled;
    private Boolean weeklyDigestEnabled;
    private Boolean readingRemindersEnabled;
    private Boolean achievementNotificationsEnabled;

    public static NotificationPreferencesResponse fromEntity(NotificationPreferences prefs) {
        return NotificationPreferencesResponse.builder()
                .newsletterEnabled(prefs.getNewsletterEnabled())
                .newBooksEnabled(prefs.getNewBooksEnabled())
                .weeklyDigestEnabled(prefs.getWeeklyDigestEnabled())
                .readingRemindersEnabled(prefs.getReadingRemindersEnabled())
                .achievementNotificationsEnabled(prefs.getAchievementNotificationsEnabled())
                .build();
    }
}