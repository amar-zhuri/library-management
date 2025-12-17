package com.library.library_management.service;

import com.library.library_management.dto.response.InsightResponse;
import com.library.library_management.repository.BookRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AIAdminInsightsService {

    private final BookRepository bookRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openai.api.key}")
    private String openAIApiKey;

    @Value("${openai.api.url}")
    private String openAIApiUrl;

    @Value("${openai.model}")
    private String openAIModel;

    public InsightResponse getSystemAIInsights() {
        long start = System.currentTimeMillis();

        if (openAIApiKey == null || openAIApiKey.isBlank()) {
            return InsightResponse.builder()
                    .insights(List.of("AI insights not available. OpenAI API key not configured."))
                    .generatedBy("AI")
                    .generatedAtMs(System.currentTimeMillis())
                    .build();
        }

        String profile = buildSystemProfile();
        if (profile.isBlank()) {
            return InsightResponse.builder()
                    .insights(List.of("No books in the system yet. Add books to see AI insights."))
                    .generatedBy("AI")
                    .generatedAtMs(System.currentTimeMillis())
                    .build();
        }

        String prompt = buildPrompt(profile);
        String aiResponse = callOpenAI(prompt);

        return parseResponse(aiResponse, start);
    }

    private String buildSystemProfile() {
        StringBuilder sb = new StringBuilder();

        long totalBooks = bookRepository.count();
        if (totalBooks == 0) {
            return "";
        }

        sb.append("SYSTEM LIBRARY DATA\n");
        sb.append("- Total books: ").append(totalBooks).append("\n");

        List<Object[]> genres = bookRepository.countBooksByGenre();
        if (!genres.isEmpty()) {
            sb.append("- Genres:\n");
            for (Object[] row : genres) {
                sb.append("  • ").append(row[0]).append(": ").append(row[1]).append("\n");
            }
        }

        List<Object[]> statuses = bookRepository.countBooksByStatus();
        if (!statuses.isEmpty()) {
            sb.append("- Statuses:\n");
            for (Object[] row : statuses) {
                sb.append("  • ").append(row[0]).append(": ").append(row[1]).append("\n");
            }
        }

        List<Object[]> topAuthors = bookRepository.findTopAuthors(PageRequest.of(0, 5));
        if (!topAuthors.isEmpty()) {
            sb.append("- Top authors:\n");
            for (Object[] row : topAuthors) {
                sb.append("  • ").append(row[0]).append(": ").append(row[1]).append(" books\n");
            }
        }

        List<Object[]> topReaders = bookRepository.countBooksPerUser();
        if (!topReaders.isEmpty()) {
            sb.append("- Top readers:\n");
            for (Object[] row : topReaders.stream().limit(5).collect(Collectors.toList())) {
                sb.append("  • ").append(row[1]).append(": ").append(row[2]).append(" books\n");
            }
        }

        return sb.toString();
    }

    private String buildPrompt(String profile) {
        return """
            You are an analytics assistant for a library system. Using the aggregated library data below, produce 4-6 concise insights about trends in the entire library (not about a single user).

            Focus on:
            - Genre distribution and trends
            - Reading status mix (to-read/reading/completed)
            - Top authors and readers
            - Any notable imbalances or opportunities

            %s

            Format EXACTLY:
            INSIGHT:First insight
            INSIGHT:Second insight
            INSIGHT:Third insight
            INSIGHT:Fourth insight
            """.formatted(profile);
    }

    private String callOpenAI(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAIApiKey);

            Map<String, Object> body = Map.of(
                    "model", openAIModel,
                    "messages", List.of(
                            Map.of("role", "system", "content", "You are a helpful analytics assistant. Follow the requested format."),
                            Map.of("role", "user", "content", prompt)
                    ),
                    "max_tokens", 400,
                    "temperature", 0.6
            );

            ResponseEntity<String> response = restTemplate.exchange(
                    openAIApiUrl,
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    String.class
            );

            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            log.error("AI insights call failed: {}", e.getMessage());
            return "";
        }
    }

    private InsightResponse parseResponse(String aiResponse, long start) {
        List<String> insights = new ArrayList<>();
        String[] lines = aiResponse.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("INSIGHT:")) {
                insights.add(line.replace("INSIGHT:", "").trim());
            }
        }
        if (insights.isEmpty()) {
            insights.add("AI could not generate insights. Please try again later.");
        }
        return InsightResponse.builder()
                .insights(insights)
                .generatedBy("AI")
                .generatedAtMs(System.currentTimeMillis())
                .build();
    }
}
