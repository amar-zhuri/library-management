package com.library.library_management.service;

import com.library.library_management.dto.response.RecommendationResponse;
import com.library.library_management.dto.response.RecommendationResponse.RecommendedBook;
import com.library.library_management.entity.Book;
import com.library.library_management.entity.enums.Genre;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AIRecommendationService {

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
     * Get AI-powered recommendations using OpenAI
     */
    public RecommendationResponse getAIRecommendations(Long userId) {
        log.info("Generating AI recommendations for user {}", userId);

        // Check if OpenAI is configured
        if (openAIApiKey == null || openAIApiKey.isBlank()) {
            log.warn("OpenAI API key not configured");
            return buildEmptyResponse("AI recommendations not available. OpenAI API key not configured.");
        }

        // Gather user's reading profile
        String userProfile = buildUserProfile(userId);

        // Gather available books (that user doesn't own)
        String availableBooks = getAvailableBooksContext(userId);

        // If no data, return empty
        if (userProfile.isEmpty()) {
            return buildEmptyResponse("Add more books to get AI recommendations!");
        }

        if (availableBooks.isEmpty()) {
            return buildEmptyResponse("No other books available in the library for recommendations.");
        }

        // Ask OpenAI
        String prompt = buildAIPrompt(userProfile, availableBooks);
        String aiResponse = callOpenAI(prompt);

        if (aiResponse.isEmpty()) {
            return buildEmptyResponse("AI service temporarily unavailable. Try again later.");
        }

        // Parse AI response into recommendations
        List<RecommendedBook> aiPicks = parseAIResponse(aiResponse, userId);

        return RecommendationResponse.builder()
                .byGenre(List.of())
                .byAuthor(List.of())
                .fromSimilarUsers(aiPicks)
                .message("AI-powered recommendations based on your reading profile!")
                .build();
    }

    /**
     * Build user's reading profile for AI
     */
    private String buildUserProfile(Long userId) {
        StringBuilder profile = new StringBuilder();

        // Total books
        long totalBooks = bookRepository.countByUserId(userId);
        if (totalBooks == 0) {
            return "";
        }

        profile.append("User's Reading Profile:\n");
        profile.append("- Total books owned: ").append(totalBooks).append("\n");

        // Genre distribution
        List<Object[]> genres = bookRepository.countBooksByGenreForUser(userId);
        if (!genres.isEmpty()) {
            profile.append("- Favorite genres: ");
            for (int i = 0; i < Math.min(3, genres.size()); i++) {
                Genre genre = (Genre) genres.get(i)[0];
                Long count = (Long) genres.get(i)[1];
                int percentage = (int) ((count * 100) / totalBooks);
                profile.append(genre).append(" (").append(percentage).append("%)");
                if (i < Math.min(3, genres.size()) - 1) {
                    profile.append(", ");
                }
            }
            profile.append("\n");
        }

        // Favorite authors
        List<Object[]> authors = bookRepository.findTopAuthorsForUser(userId, PageRequest.of(0, 3));
        if (!authors.isEmpty()) {
            profile.append("- Favorite authors: ");
            for (int i = 0; i < authors.size(); i++) {
                String author = (String) authors.get(i)[0];
                Long count = (Long) authors.get(i)[1];
                profile.append(author).append(" (").append(count).append(" books)");
                if (i < authors.size() - 1) {
                    profile.append(", ");
                }
            }
            profile.append("\n");
        }

        // Reading completion status
        List<Object[]> statuses = bookRepository.countBooksByStatusForUser(userId);
        long completed = 0;
        for (Object[] row : statuses) {
            if (row[0].toString().equals("COMPLETED")) {
                completed = (Long) row[1];
            }
        }
        int completionRate = (int) ((completed * 100) / totalBooks);
        profile.append("- Books completed: ").append(completed).append(" (").append(completionRate).append("%)\n");

        // Sample of user's books
        List<Book> userBooks = bookRepository.findRecentBooksForUser(userId, PageRequest.of(0, 5));
        if (!userBooks.isEmpty()) {
            profile.append("- Books they own: ");
            profile.append(userBooks.stream()
                    .map(b -> "\"" + b.getTitle() + "\" by " + b.getAuthor())
                    .collect(Collectors.joining(", ")));
            profile.append("\n");
        }

        return profile.toString();
    }

    /**
     * Get available books that user doesn't own
     */
    /**
     * Get available books that user doesn't own (by title/author, not just ID)
     */
    private String getAvailableBooksContext(Long userId) {
        // Get user's book titles to exclude
        List<Book> userBooks = bookRepository.findByUserId(userId);
        List<String> userBookKeys = userBooks.stream()
                .map(b -> b.getTitle().toLowerCase() + "|" + b.getAuthor().toLowerCase())
                .collect(Collectors.toList());

        // Get all books not owned by user (by title/author combination)
        List<Book> allBooks = bookRepository.findAll();
        List<Book> availableBooks = allBooks.stream()
                .filter(b -> !b.getUser().getId().equals(userId))
                .filter(b -> !userBookKeys.contains(
                        b.getTitle().toLowerCase() + "|" + b.getAuthor().toLowerCase()))
                .limit(15)
                .collect(Collectors.toList());

        if (availableBooks.isEmpty()) {
            return "";
        }

        StringBuilder context = new StringBuilder("Available books to recommend from:\n");
        for (Book book : availableBooks) {
            context.append("- ID:").append(book.getId())
                    .append(" | \"").append(book.getTitle()).append("\"")
                    .append(" by ").append(book.getAuthor())
                    .append(" | Genre: ").append(book.getGenre());
            if (book.getPageCount() != null) {
                context.append(" | ").append(book.getPageCount()).append(" pages");
            }
            context.append("\n");
        }

        return context.toString();
    }

    /**
     * Build prompt for OpenAI
     */
    private String buildAIPrompt(String userProfile, String availableBooks) {
        return """
            You are a book recommendation assistant. Based on the user's reading profile,
            suggest 3-5 books from the available list that they would enjoy.
            
            %s
            
            %s
            
            IMPORTANT: Only recommend books from the "Available books" list above.
            
            For each recommendation, explain WHY this specific user would enjoy it based on their profile.
            
            Format your response EXACTLY like this (one book per line):
            ID:1|REASON:Your personalized reason here explaining why they'd enjoy this book.
            ID:5|REASON:Another personalized reason based on their reading habits.
            
            Only output lines in this format. No other text.
            """.formatted(userProfile, availableBooks);
    }

    /**
     * Call OpenAI API
     */
    private String callOpenAI(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAIApiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", openAIModel);
            requestBody.put("messages", List.of(
                    Map.of("role", "system", "content", "You are a helpful book recommendation assistant. Always follow the exact format requested."),
                    Map.of("role", "user", "content", prompt)
            ));
            requestBody.put("max_tokens", 500);
            requestBody.put("temperature", 0.7);

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
     * Parse AI response into recommendations
     */
    private List<RecommendedBook> parseAIResponse(String aiResponse, Long userId) {
        List<RecommendedBook> recommendations = new ArrayList<>();

        if (aiResponse == null || aiResponse.isEmpty()) {
            return recommendations;
        }

        String[] lines = aiResponse.split("\n");
        for (String line : lines) {
            try {
                line = line.trim();
                if (line.startsWith("ID:") && line.contains("|REASON:")) {
                    String[] parts = line.split("\\|REASON:");
                    if (parts.length == 2) {
                        String idPart = parts[0].replace("ID:", "").trim();
                        String reason = parts[1].trim();

                        Long bookId = Long.parseLong(idPart);
                        Book book = bookRepository.findById(bookId).orElse(null);

                        // Make sure book exists and user doesn't own it
                        if (book != null && !book.getUser().getId().equals(userId)) {
                            recommendations.add(RecommendedBook.builder()
                                    .id(book.getId())
                                    .title(book.getTitle())
                                    .author(book.getAuthor())
                                    .genre(book.getGenre())
                                    .pageCount(book.getPageCount())
                                    .publicationYear(book.getPublicationYear())
                                    .reason(reason)
                                    .build());
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("Could not parse line: {}", line);
            }
        }

        log.info("Parsed {} AI recommendations", recommendations.size());
        return recommendations;
    }

    /**
     * Build empty response with message
     */
    private RecommendationResponse buildEmptyResponse(String message) {
        return RecommendationResponse.builder()
                .byGenre(List.of())
                .byAuthor(List.of())
                .fromSimilarUsers(List.of())
                .message(message)
                .build();
    }
}