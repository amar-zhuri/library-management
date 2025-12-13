package com.library.library_management.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.library_management.dto.response.AIQueryResponse;
import com.library.library_management.entity.enums.QueryType;
import com.library.library_management.repository.BookRepository;
import com.library.library_management.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Slf4j
@ConditionalOnProperty(name = "openai.api.key")
public class OpenAIService implements LLMService {

    @Value("${openai.api.key:}")
    private String apiKey;

    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;

    @Value("${openai.model:gpt-3.5-turbo}")
    private String model;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public OpenAIService(BookRepository bookRepository, UserRepository userRepository) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    @Override
    public AIQueryResponse processQuery(String question, Long userId) {
        log.info("Processing query with OpenAI: '{}'", question);

        try {
            // Build context about the database
            String context = buildDatabaseContext(userId);

            // Build the prompt
            String systemPrompt = """
                    You are a helpful library assistant. You have access to a library management system database.
                    Answer questions about books, reading statistics, and library data.
                    
                    Database context:
                    %s
                    
                    Respond in a friendly, conversational manner. If you cannot answer the question based on 
                    the provided context, say so politely and suggest what questions you can answer.
                    
                    Keep responses concise but informative.
                    """.formatted(context);

            String response = callOpenAI(systemPrompt, question);

            return AIQueryResponse.builder()
                    .queryType(QueryType.UNKNOWN) // LLM doesn't map to specific types
                    .answer(response)
                    .recognizedQuery(true)
                    .confidence(0.8)
                    .processingMethod("LLM")
                    .build();

        } catch (Exception e) {
            log.error("OpenAI query failed: {}", e.getMessage());
            return AIQueryResponse.builder()
                    .queryType(QueryType.UNKNOWN)
                    .answer("I'm having trouble processing your question right now. Please try again later.")
                    .recognizedQuery(false)
                    .confidence(0.0)
                    .processingMethod("LLM_ERROR")
                    .build();
        }
    }

    @Override
    public String generateInsight(String context) {
        String systemPrompt = """
                You are a data analyst for a library management system.
                Based on the following statistics, generate 3-5 interesting insights about the library.
                Be specific and use the actual numbers provided.
                
                Statistics:
                %s
                """.formatted(context);

        return callOpenAI(systemPrompt, "Generate insights about this library data.");
    }

    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.isBlank();
    }

    private String buildDatabaseContext(Long userId) {
        StringBuilder context = new StringBuilder();

        // System-wide stats
        long totalBooks = bookRepository.count();
        long totalUsers = userRepository.count();
        context.append(String.format("Total books in system: %d\n", totalBooks));
        context.append(String.format("Total users: %d\n", totalUsers));

        // Genre distribution
        context.append("Books by genre: ");
        bookRepository.countBooksByGenre().forEach(row ->
                context.append(String.format("%s=%d, ", row[0], row[1])));
        context.append("\n");

        // Status distribution
        context.append("Books by status: ");
        bookRepository.countBooksByStatus().forEach(row ->
                context.append(String.format("%s=%d, ", row[0], row[1])));
        context.append("\n");

        // User-specific context
        if (userId != null) {
            long userBooks = bookRepository.countByUserId(userId);
            context.append(String.format("\nCurrent user has %d books.\n", userBooks));

            context.append("User's books by genre: ");
            bookRepository.countBooksByGenreForUser(userId).forEach(row ->
                    context.append(String.format("%s=%d, ", row[0], row[1])));
            context.append("\n");
        }

        // Top readers
        context.append("Top readers: ");
        bookRepository.countBooksPerUser().stream().limit(3).forEach(row ->
                context.append(String.format("%s has %d books, ", row[1], row[2])));
        context.append("\n");

        return context.toString();
    }

    private String callOpenAI(String systemPrompt, String userMessage) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userMessage)
        ));
        requestBody.put("max_tokens", 500);
        requestBody.put("temperature", 0.7);

        try {
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl, HttpMethod.POST, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("choices").get(0).path("message").path("content").asText();

        } catch (Exception e) {
            log.error("OpenAI API call failed: {}", e.getMessage());
            throw new RuntimeException("Failed to call OpenAI API", e);
        }
    }
}