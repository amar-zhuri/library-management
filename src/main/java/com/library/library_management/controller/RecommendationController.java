package com.library.library_management.controller;

import com.library.library_management.dto.response.RecommendationResponse;
import com.library.library_management.dto.response.RecommendationResponse.RecommendedBook;
import com.library.library_management.security.CustomUserDetails;
import com.library.library_management.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@Slf4j
public class RecommendationController {

    private final RecommendationService recommendationService;

    /**
     * Get all recommendations for the current user
     * GET /api/recommendations
     */
    @GetMapping
    public ResponseEntity<RecommendationResponse> getRecommendations(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("GET /api/recommendations for user {}", userDetails.getId());

        RecommendationResponse response = recommendationService.getRecommendations(userDetails.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Get recommendations by favorite genre
     * GET /api/recommendations/by-genre
     */
    @GetMapping("/by-genre")
    public ResponseEntity<List<RecommendedBook>> getByGenre(
            @RequestParam(defaultValue = "5") int limit,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("GET /api/recommendations/by-genre for user {}", userDetails.getId());

        List<RecommendedBook> recommendations = recommendationService
                .getRecommendationsByGenre(userDetails.getId(), limit);

        return ResponseEntity.ok(recommendations);
    }

    /**
     * Get recommendations by favorite authors
     * GET /api/recommendations/by-author
     */
    @GetMapping("/by-author")
    public ResponseEntity<List<RecommendedBook>> getByAuthor(
            @RequestParam(defaultValue = "5") int limit,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("GET /api/recommendations/by-author for user {}", userDetails.getId());

        List<RecommendedBook> recommendations = recommendationService
                .getRecommendationsByAuthor(userDetails.getId(), limit);

        return ResponseEntity.ok(recommendations);
    }

    /**
     * Get recommendations from similar users
     * GET /api/recommendations/discover
     */
    @GetMapping("/discover")
    public ResponseEntity<List<RecommendedBook>> getFromSimilarUsers(
            @RequestParam(defaultValue = "5") int limit,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("GET /api/recommendations/discover for user {}", userDetails.getId());

        List<RecommendedBook> recommendations = recommendationService
                .getRecommendationsFromSimilarUsers(userDetails.getId(), limit);

        return ResponseEntity.ok(recommendations);
    }
}