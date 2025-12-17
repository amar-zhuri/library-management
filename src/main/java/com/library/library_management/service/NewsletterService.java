package com.library.library_management.service;

import com.library.library_management.dto.request.NewsletterRequest;
import com.library.library_management.dto.response.NewsletterResponse;
import com.library.library_management.dto.response.PagedResponse;
import com.library.library_management.entity.Newsletter;
import com.library.library_management.entity.NotificationPreferences;
import com.library.library_management.entity.User;
import com.library.library_management.entity.enums.NewsletterStatus;
import com.library.library_management.exception.ResourceNotFoundException;
import com.library.library_management.repository.NewsletterRepository;
import com.library.library_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsletterService {

    private final NewsletterRepository newsletterRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;

    /**
     * Create a new newsletter (draft)
     */
    @Transactional
    public NewsletterResponse createNewsletter(NewsletterRequest request, Long adminId) {
        log.info("Creating newsletter: {}", request.getSubject());

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("User", adminId));

        Newsletter newsletter = Newsletter.builder()
                .subject(request.getSubject())
                .content(request.getContent())
                .status(NewsletterStatus.DRAFT)
                .createdBy(admin)
                .build();

        newsletter = newsletterRepository.save(newsletter);
        log.info("Newsletter created with id: {}", newsletter.getId());

        return NewsletterResponse.fromEntity(newsletter);
    }

    /**
     * Update a newsletter (only drafts can be updated)
     */
    @Transactional
    public NewsletterResponse updateNewsletter(Long id, NewsletterRequest request) {
        log.info("Updating newsletter: {}", id);

        Newsletter newsletter = newsletterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Newsletter", id));

        if (newsletter.getStatus() != NewsletterStatus.DRAFT) {
            throw new IllegalStateException("Can only update draft newsletters");
        }

        newsletter.setSubject(request.getSubject());
        newsletter.setContent(request.getContent());

        newsletter = newsletterRepository.save(newsletter);
        return NewsletterResponse.fromEntity(newsletter);
    }

    /**
     * Get newsletter by ID
     */
    @Transactional(readOnly = true)
    public NewsletterResponse getNewsletter(Long id) {
        Newsletter newsletter = newsletterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Newsletter", id));
        return NewsletterResponse.fromEntity(newsletter);
    }

    /**
     * Get all newsletters with pagination
     */
    @Transactional(readOnly = true)
    public PagedResponse<NewsletterResponse> getAllNewsletters(Pageable pageable) {
        Page<Newsletter> page = newsletterRepository.findAllByOrderByCreatedAtDesc(pageable);
        Page<NewsletterResponse> responsePage = page.map(NewsletterResponse::fromEntity);
        return PagedResponse.fromPage(responsePage);
    }

    /**
     * Delete a newsletter (only drafts can be deleted)
     */
    @Transactional
    public void deleteNewsletter(Long id) {
        log.info("Deleting newsletter: {}", id);

        Newsletter newsletter = newsletterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Newsletter", id));

        if (newsletter.getStatus() != NewsletterStatus.DRAFT) {
            throw new IllegalStateException("Can only delete draft newsletters");
        }

        newsletterRepository.delete(newsletter);
        log.info("Newsletter deleted: {}", id);
    }

    /**
     * Send newsletter to all subscribers
     */
    @Transactional
    public NewsletterResponse sendNewsletter(Long id) {
        log.info("Sending newsletter: {}", id);

        Newsletter newsletter = newsletterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Newsletter", id));

        if (newsletter.getStatus() != NewsletterStatus.DRAFT) {
            throw new IllegalStateException("Newsletter has already been sent or is being sent");
        }

        // Mark as sending
        newsletter.setStatus(NewsletterStatus.SENDING);
        newsletterRepository.save(newsletter);

        // Send asynchronously
        sendNewsletterAsync(newsletter);

        return NewsletterResponse.fromEntity(newsletter);
    }

    /**
     * Async method to send newsletter to all subscribers
     */
    @Async
    protected void sendNewsletterAsync(Newsletter newsletter) {
        log.info("Starting async newsletter send for: {}", newsletter.getId());

        try {
            List<NotificationPreferences> subscribers = notificationService.getNewsletterSubscribers();
            int sentCount = 0;

            for (NotificationPreferences prefs : subscribers) {
                User user = prefs.getUser();
                try {
                    emailService.sendNewsletter(
                            user.getEmail(),
                            user.getName(),
                            newsletter.getSubject(),
                            newsletter.getContent(),
                            prefs.getUnsubscribeToken()
                    );
                    sentCount++;
                } catch (Exception e) {
                    log.error("Failed to send newsletter to {}: {}", user.getEmail(), e.getMessage());
                }
            }

            // Update newsletter status
            newsletter.setStatus(NewsletterStatus.SENT);
            newsletter.setSentAt(LocalDateTime.now());
            newsletter.setRecipientCount(sentCount);
            newsletterRepository.save(newsletter);

            log.info("Newsletter {} sent to {} subscribers", newsletter.getId(), sentCount);

        } catch (Exception e) {
            log.error("Failed to send newsletter: {}", e.getMessage());
            newsletter.setStatus(NewsletterStatus.FAILED);
            newsletterRepository.save(newsletter);
        }
    }

    /**
     * Get newsletter statistics
     */
    public NewsletterStats getStats() {
        long totalNewsletters = newsletterRepository.count();
        long sentNewsletters = newsletterRepository.findByStatusOrderByCreatedAtDesc(NewsletterStatus.SENT).size();
        long subscriberCount = notificationService.getNewsletterSubscriberCount();

        return NewsletterStats.builder()
                .totalNewsletters(totalNewsletters)
                .sentNewsletters(sentNewsletters)
                .draftNewsletters(totalNewsletters - sentNewsletters)
                .subscriberCount(subscriberCount)
                .build();
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class NewsletterStats {
        private long totalNewsletters;
        private long sentNewsletters;
        private long draftNewsletters;
        private long subscriberCount;
    }
}
