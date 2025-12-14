package com.library.library_management.service;

import com.library.library_management.dto.request.NotificationPreferencesRequest;
import com.library.library_management.dto.response.NotificationPreferencesResponse;
import com.library.library_management.entity.Book;
import com.library.library_management.entity.NotificationPreferences;
import com.library.library_management.entity.User;
import com.library.library_management.exception.ResourceNotFoundException;
import com.library.library_management.repository.NotificationPreferencesRepository;
import com.library.library_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationPreferencesRepository preferencesRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    /**
     * Get user's notification preferences (create default if not exists)
     */
    @Transactional
    public NotificationPreferencesResponse getPreferences(Long userId) {
        NotificationPreferences prefs = getOrCreatePreferences(userId);
        return NotificationPreferencesResponse.fromEntity(prefs);
    }

    /**
     * Update user's notification preferences
     */
    @Transactional
    public NotificationPreferencesResponse updatePreferences(Long userId, NotificationPreferencesRequest request) {
        log.info("Updating notification preferences for user {}", userId);

        NotificationPreferences prefs = getOrCreatePreferences(userId);

        if (request.getNewsletterEnabled() != null) {
            prefs.setNewsletterEnabled(request.getNewsletterEnabled());
        }
        if (request.getNewBooksEnabled() != null) {
            prefs.setNewBooksEnabled(request.getNewBooksEnabled());
        }
        if (request.getWeeklyDigestEnabled() != null) {
            prefs.setWeeklyDigestEnabled(request.getWeeklyDigestEnabled());
        }
        if (request.getReadingRemindersEnabled() != null) {
            prefs.setReadingRemindersEnabled(request.getReadingRemindersEnabled());
        }
        if (request.getAchievementNotificationsEnabled() != null) {
            prefs.setAchievementNotificationsEnabled(request.getAchievementNotificationsEnabled());
        }

        prefs = preferencesRepository.save(prefs);
        log.info("Notification preferences updated for user {}", userId);

        return NotificationPreferencesResponse.fromEntity(prefs);
    }

    /**
     * Unsubscribe from all emails using token
     */
    @Transactional
    public void unsubscribeAll(String token) {
        log.info("Unsubscribing user with token");

        NotificationPreferences prefs = preferencesRepository.findByUnsubscribeToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid unsubscribe token"));

        prefs.setNewsletterEnabled(false);
        prefs.setNewBooksEnabled(false);
        prefs.setWeeklyDigestEnabled(false);
        prefs.setReadingRemindersEnabled(false);
        prefs.setAchievementNotificationsEnabled(false);

        preferencesRepository.save(prefs);
        log.info("User {} unsubscribed from all notifications", prefs.getUser().getEmail());
    }

    /**
     * Unsubscribe from newsletter only using token
     */
    @Transactional
    public void unsubscribeNewsletter(String token) {
        log.info("Unsubscribing user from newsletter with token");

        NotificationPreferences prefs = preferencesRepository.findByUnsubscribeToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid unsubscribe token"));

        prefs.setNewsletterEnabled(false);
        preferencesRepository.save(prefs);
        log.info("User {} unsubscribed from newsletter", prefs.getUser().getEmail());
    }

    /**
     * Send new book notification to all subscribers
     */
    @Transactional(readOnly = true)
    public void notifyNewBook(Book book, User addedBy) {
        log.info("Sending new book notifications for: {}", book.getTitle());

        List<NotificationPreferences> subscribers = preferencesRepository.findAllNewBookSubscribers();

        int sentCount = 0;
        for (NotificationPreferences prefs : subscribers) {
            User user = prefs.getUser();
            // Don't notify the user who added the book
            if (!user.getId().equals(addedBy.getId())) {
                try {
                    emailService.sendNewBookNotification(
                            user.getEmail(),
                            user.getName(),
                            book,
                            addedBy.getName(),
                            prefs.getUnsubscribeToken()
                    );
                    sentCount++;
                } catch (Exception e) {
                    log.error("Failed to send new book notification to {}: {}", user.getEmail(), e.getMessage());
                }
            }
        }

        log.info("New book notification sent to {} subscribers", sentCount);
    }

    /**
     * Get or create notification preferences for a user
     */
    private NotificationPreferences getOrCreatePreferences(Long userId) {
        return preferencesRepository.findById(userId)
                .orElseGet(() -> createDefaultPreferences(userId));
    }

    /**
     * Create default notification preferences for a new user
     */
    @Transactional
    public NotificationPreferences createDefaultPreferences(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        NotificationPreferences prefs = NotificationPreferences.builder()
                .user(user)
                .newsletterEnabled(true)
                .newBooksEnabled(true)
                .weeklyDigestEnabled(false)
                .readingRemindersEnabled(false)
                .achievementNotificationsEnabled(true)
                .unsubscribeToken(generateUnsubscribeToken())
                .build();

        return preferencesRepository.save(prefs);
    }

    /**
     * Get subscriber count for newsletter
     */
    public long getNewsletterSubscriberCount() {
        return preferencesRepository.countNewsletterSubscribers();
    }

    /**
     * Get all newsletter subscribers
     */
    public List<NotificationPreferences> getNewsletterSubscribers() {
        return preferencesRepository.findAllNewsletterSubscribers();
    }

    /**
     * Generate unique unsubscribe token
     */
    private String generateUnsubscribeToken() {
        return UUID.randomUUID().toString();
    }
}