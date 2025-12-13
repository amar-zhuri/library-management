package com.library.library_management.controller;

import com.library.library_management.dto.request.AIQueryRequest;
import com.library.library_management.dto.response.AIQueryResponse;
import com.library.library_management.dto.response.InsightResponse;
import com.library.library_management.security.CustomUserDetails;
import com.library.library_management.service.ai.AIQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
public class AIQueryController {

    private final AIQueryService aiQueryService;

    /**
     * Process a natural language query
     * POST /api/ai/query
     */
    @PostMapping("/query")
    public ResponseEntity<AIQueryResponse> processQuery(
            @Valid @RequestBody AIQueryRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("POST /api/ai/query - User {} asking: '{}'",
                userDetails.getId(), request.getQuestion());

        AIQueryResponse response = aiQueryService.processQuery(
                request.getQuestion(),
                userDetails.getId(),
                //request.isUseLLM()
                request.getUseLLM()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get suggested queries
     * GET /api/ai/suggestions
     */
    @GetMapping("/suggestions")
    public ResponseEntity<Map<String, List<String>>> getSuggestions() {
        log.info("GET /api/ai/suggestions");

        List<String> suggestions = aiQueryService.getSuggestions();

        return ResponseEntity.ok(Map.of(
                "suggestions", suggestions,
                "categories", List.of(
                        "User queries (about your library)",
                        "System queries (overall statistics)",
                        "Recommendations"
                )
        ));
    }

    /**
     * Get AI-generated insights (Admin only)
     * GET /api/ai/insights
     */
    @GetMapping("/insights")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InsightResponse> getInsights() {
        log.info("GET /api/ai/insights - Generating system insights");

        InsightResponse response = aiQueryService.generateInsights();
        return ResponseEntity.ok(response);
    }

    /**
     * Quick stats endpoint for common queries
     * GET /api/ai/quick-stats
     */
    @GetMapping("/quick-stats")
    public ResponseEntity<Map<String, Object>> getQuickStats(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("GET /api/ai/quick-stats for user {}", userDetails.getId());

        // Execute several common queries and return combined results
        AIQueryResponse bookCount = aiQueryService.processQuery(
                "How many books do I have?", userDetails.getId(), false);
        AIQueryResponse genreStats = aiQueryService.processQuery(
                "What genres do I read most?", userDetails.getId(), false);
        AIQueryResponse readingStats = aiQueryService.processQuery(
                "Show my reading statistics", userDetails.getId(), false);

        Map<String, Object> quickStats = new LinkedHashMap<>();
        quickStats.put("bookCount", bookCount.getData());
        quickStats.put("favoriteGenres", genreStats.getData());
        quickStats.put("readingProgress", readingStats.getData());

        return ResponseEntity.ok(quickStats);
    }
}
