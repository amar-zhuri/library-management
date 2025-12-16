package com.library.library_management.service;

import com.library.library_management.dto.response.UserInsightsResponse;
import com.library.library_management.entity.Book;
import com.library.library_management.entity.enums.Genre;
import com.library.library_management.entity.enums.ReadingStatus;
import com.library.library_management.repository.BookRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AIUserInsightsService {

    private final BookRepository bookRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openai.api.key}")
    private String openAIApiKey;

    @Value("${openai.api.url}")
    private String openAIApiUrl;

    @Value("${openai.model}")
    private String openAIModel;

    /**
     * Generate AI-powered insights for a user
     */
    public UserInsightsResponse getAIInsights(Long userId) {
        long startTime = System.currentTimeMillis();
        log.info("Generating AI insights for user {}", userId);

        // Check if OpenAI is configured
        if (openAIApiKey == null || openAIApiKey.isBlank()) {
            log.warn("OpenAI API key not configured");
            return buildErrorResponse("AI insights not available. OpenAI API key not configured.", startTime);
        }

        // Gather user's reading data
        String userProfile = buildUserProfile(userId);

        if (userProfile.isEmpty()) {
            return buildErrorResponse("Add more books to get AI-powered insights!", startTime);
        }

        // Ask OpenAI for insights
        String prompt = buildPrompt(userProfile);
        String aiResponse = callOpenAI(prompt);

        if (aiResponse.isEmpty()) {
            return buildErrorResponse("AI service temporarily unavailable. Try again later.", startTime);
        }

        // Parse AI response
        return parseAIResponse(aiResponse, startTime);
    }

    /**
     * Build detailed user profile for AI analysis
     */
    private String buildUserProfile(Long userId) {
        StringBuilder profile = new StringBuilder();

        // Total books
        long totalBooks = bookRepository.countByUserId(userId);
        if (totalBooks == 0) {
            return "";
        }

        profile.append("USER'S READING DATA:\n\n");
        profile.append("Total books owned: ").append(totalBooks).append("\n\n");

        // Genre breakdown
        List<Object[]> genreStats = bookRepository.countBooksByGenreForUser(userId);
        if (!genreStats.isEmpty()) {
            profile.append("Genre Distribution:\n");
            for (Object[] row : genreStats) {
                Genre genre = (Genre) row[0];
                Long count = (Long) row[1];
                int percentage = (int) ((count * 100) / totalBooks);
                profile.append("- ").append(genre).append(": ").append(count)
                        .append(" books (").append(percentage).append("%)\n");
            }
            profile.append("\n");
        }

        // Author breakdown
        List<Object[]> authorStats = bookRepository.findTopAuthorsForUser(userId, PageRequest.of(0, 5));
        if (!authorStats.isEmpty()) {
            profile.append("Top Authors:\n");
            for (Object[] row : authorStats) {
                String author = (String) row[0];
                Long count = (Long) row[1];
                profile.append("- ").append(author).append(": ").append(count).append(" books\n");
            }
            profile.append("\n");
        }

        // Reading status breakdown
        List<Object[]> statusStats = bookRepository.countBooksByStatusForUser(userId);
        if (!statusStats.isEmpty()) {
            profile.append("Reading Status:\n");
            for (Object[] row : statusStats) {
                ReadingStatus status = (ReadingStatus) row[0];
                Long count = (Long) row[1];
                int percentage = (int) ((count * 100) / totalBooks);
                profile.append("- ").append(status).append(": ").append(count)
                        .append(" books (").append(percentage).append("%)\n");
            }
            profile.append("\n");
        }

        // Library value
        BigDecimal totalValue = bookRepository.calculateLibraryValueForUser(userId);
        if (totalValue != null && totalValue.compareTo(BigDecimal.ZERO) > 0) {
            profile.append("Total Library Value: $").append(totalValue).append("\n\n");
        }

        // Recent books
        List<Book> recentBooks = bookRepository.findRecentBooksForUser(userId, PageRequest.of(0, 5));
        if (!recentBooks.isEmpty()) {
            profile.append("Recent Books:\n");
            for (Book book : recentBooks) {
                profile.append("- \"").append(book.getTitle()).append("\" by ").append(book.getAuthor())
                        .append(" (").append(book.getGenre()).append(", ").append(book.getStatus()).append(")");
                if (book.getPageCount() != null) {
                    profile.append(" - ").append(book.getPageCount()).append(" pages");
                }
                profile.append("\n");
            }
        }

        return profile.toString();
    }

    /**
     * Build prompt for OpenAI
     */
    private String buildPrompt(String userProfile) {
        return """
            You are a reading habit analyst. Based on the user's library data, provide personalized insights about their reading habits and preferences.
            
            %s
            
            Analyze this data and provide:
            1. 4-6 insightful observations about their reading personality and habits
            2. A brief summary (1-2 sentences) describing them as a reader
            
            Make insights personal and interesting, not just restating numbers. Look for patterns like:
            - What kind of reader are they? (explorer, specialist, completionist, etc.)
            - What do their genre choices say about them?
            - What patterns do you see in their reading behavior?
            - Any interesting observations about book length, completion rate, etc.
            
            Format your response EXACTLY like this:
            INSIGHT:Your first insight here
            INSIGHT:Your second insight here
            INSIGHT:Your third insight here
            INSIGHT:Your fourth insight here
            SUMMARY:Your 1-2 sentence summary of them as a reader
            
            Only output lines in this format. No other text.
            """.formatted(userProfile);
    }

    /**
     * Call OpenAI API
     */
    private String callOpenAI(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAIApiKey);

            Map<String, Object> requestBody = Map.of(
                    "model", openAIModel,
                    "messages", List.of(
                            Map.of("role", "system", "content", "You are a helpful reading habit analyst. Always follow the exact format requested."),
                            Map.of("role", "user", "content", prompt)
                    ),
                    "max_tokens", 600,
                    "temperature", 0.7
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    openAIApiUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            JsonNode root = objectMapper.readTree(response.getBody());
            String content = root.path("choices").get(0).path("message").path("content").asText();

            log.debug("OpenAI response: {}", content);
            return content;

        } catch (Exception e) {
            log.error("OpenAI API call failed: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Parse AI response into UserInsightsResponse
     */
    private UserInsightsResponse parseAIResponse(String aiResponse, long startTime) {
        List<String> insights = new ArrayList<>();
        String summary = "";

        String[] lines = aiResponse.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("INSIGHT:")) {
                insights.add(line.replace("INSIGHT:", "").trim());
            } else if (line.startsWith("SUMMARY:")) {
                summary = line.replace("SUMMARY:", "").trim();
            }
        }

        // Fallback if parsing failed
        if (insights.isEmpty()) {
            insights.add("Unable to generate insights. Please try again.");
        }

        if (summary.isEmpty()) {
            summary = "Your reading profile is unique!";
        }

        long generationTime = System.currentTimeMillis() - startTime;
        log.info("Generated {} AI insights in {}ms", insights.size(), generationTime);

        return UserInsightsResponse.builder()
                .insights(insights)
                .summary(summary)
                .generatedBy("AI")
                .generationTimeMs(generationTime)
                .build();
    }

    /**
     * Build error response
     */
    private UserInsightsResponse buildErrorResponse(String message, long startTime) {
        return UserInsightsResponse.builder()
                .insights(List.of(message))
                .summary("")
                .generatedBy("AI")
                .generationTimeMs(System.currentTimeMillis() - startTime)
                .build();
    }
}