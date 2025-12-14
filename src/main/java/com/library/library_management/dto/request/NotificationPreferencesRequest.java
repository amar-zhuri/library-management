package com.library.library_management.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferencesRequest {

    private Boolean newsletterEnabled;
    private Boolean newBooksEnabled;
    private Boolean weeklyDigestEnabled;
    private Boolean readingRemindersEnabled;
    private Boolean achievementNotificationsEnabled;
}