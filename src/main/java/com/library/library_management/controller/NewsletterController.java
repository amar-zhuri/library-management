package com.library.library_management.controller;

import com.library.library_management.dto.request.NewsletterRequest;
import com.library.library_management.dto.response.MessageResponse;
import com.library.library_management.dto.response.NewsletterResponse;
import com.library.library_management.dto.response.PagedResponse;
import com.library.library_management.security.CustomUserDetails;
import com.library.library_management.service.NewsletterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/newsletter")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class NewsletterController {

    private final NewsletterService newsletterService;

    /**
     * Create a new newsletter (draft)
     * POST /api/admin/newsletter
     */
    @PostMapping
    public ResponseEntity<NewsletterResponse> createNewsletter(
            @Valid @RequestBody NewsletterRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("POST /api/admin/newsletter - Creating newsletter");
        NewsletterResponse response = newsletterService.createNewsletter(request, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all newsletters
     * GET /api/admin/newsletter?page=0&size=10
     */
    @GetMapping
    public ResponseEntity<PagedResponse<NewsletterResponse>> getAllNewsletters(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/admin/newsletter");
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<NewsletterResponse> response = newsletterService.getAllNewsletters(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get newsletter by ID
     * GET /api/admin/newsletter/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<NewsletterResponse> getNewsletter(@PathVariable Long id) {
        log.info("GET /api/admin/newsletter/{}", id);
        NewsletterResponse response = newsletterService.getNewsletter(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Update a newsletter (draft only)
     * PUT /api/admin/newsletter/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<NewsletterResponse> updateNewsletter(
            @PathVariable Long id,
            @Valid @RequestBody NewsletterRequest request) {
        log.info("PUT /api/admin/newsletter/{}", id);
        NewsletterResponse response = newsletterService.updateNewsletter(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a newsletter (draft only)
     * DELETE /api/admin/newsletter/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNewsletter(@PathVariable Long id) {
        log.info("DELETE /api/admin/newsletter/{}", id);
        newsletterService.deleteNewsletter(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Send newsletter to all subscribers
     * POST /api/admin/newsletter/{id}/send
     */
    @PostMapping("/{id}/send")
    public ResponseEntity<NewsletterResponse> sendNewsletter(@PathVariable Long id) {
        log.info("POST /api/admin/newsletter/{}/send", id);
        NewsletterResponse response = newsletterService.sendNewsletter(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get newsletter statistics
     * GET /api/admin/newsletter/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<NewsletterService.NewsletterStats> getStats() {
        log.info("GET /api/admin/newsletter/stats");
        NewsletterService.NewsletterStats stats = newsletterService.getStats();
        return ResponseEntity.ok(stats);
    }
}