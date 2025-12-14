package com.library.library_management.repository;

import com.library.library_management.entity.NotificationPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationPreferencesRepository extends JpaRepository<NotificationPreferences, Long> {

    Optional<NotificationPreferences> findByUnsubscribeToken(String token);

    // Find all users subscribed to newsletter
    @Query("SELECT np FROM NotificationPreferences np " +
           "JOIN FETCH np.user u " +
           "WHERE np.newsletterEnabled = true AND u.emailVerified = true")
    List<NotificationPreferences> findAllNewsletterSubscribers();

    // Find all users subscribed to new book notifications
    @Query("SELECT np FROM NotificationPreferences np " +
           "JOIN FETCH np.user u " +
           "WHERE np.newBooksEnabled = true AND u.emailVerified = true")
    List<NotificationPreferences> findAllNewBookSubscribers();

    // Find all users subscribed to weekly digest
    @Query("SELECT np FROM NotificationPreferences np " +
           "JOIN FETCH np.user u " +
           "WHERE np.weeklyDigestEnabled = true AND u.emailVerified = true")
    List<NotificationPreferences> findAllWeeklyDigestSubscribers();

    // Count subscribers
    @Query("SELECT COUNT(np) FROM NotificationPreferences np " +
           "JOIN np.user u " +
           "WHERE np.newsletterEnabled = true AND u.emailVerified = true")
    long countNewsletterSubscribers();
}