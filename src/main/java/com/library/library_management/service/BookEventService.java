package com.library.library_management.service;

import com.library.library_management.entity.Book;
import com.library.library_management.entity.User;
import com.library.library_management.entity.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookEventService {

    private final NotificationService notificationService;

    /**
     * Called when a book is added by an admin
     * Sends notifications to all subscribers
     */
    @Async
    public void onBookAddedByAdmin(Book book, User admin) {
        if (admin.getRole() != Role.ADMIN) {
            log.debug("Book added by non-admin user, skipping notifications");
            return;
        }

        log.info("Admin {} added book '{}', sending notifications", admin.getEmail(), book.getTitle());
        notificationService.notifyNewBook(book, admin);
    }
}