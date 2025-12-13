package com.library.library_management.service.ai;

import com.library.library_management.dto.response.AIQueryResponse;
import com.library.library_management.entity.enums.QueryType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@ConditionalOnMissingBean(OpenAIService.class)
public class FallbackLLMService implements LLMService {

    @Override
    public AIQueryResponse processQuery(String question, Long userId) {
        log.info("LLM not configured, returning fallback response for: '{}'", question);

        return AIQueryResponse.builder()
                .queryType(QueryType.UNKNOWN)
                .answer("I couldn't understand that question with the built-in parser, and no LLM is configured. " +
                        "Please try one of the suggested questions below.")
                .recognizedQuery(false)
                .confidence(0.0)
                .processingMethod("FALLBACK")
                .suggestions(getDefaultSuggestions())
                .build();
    }

    @Override
    public String generateInsight(String context) {
        return "LLM not configured. Enable OpenAI integration for AI-generated insights.";
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    private List<String> getDefaultSuggestions() {
        return List.of(
                "Who owns the most books?",
                "Which is the most popular book?",
                "Show the five most expensive books",
                "What genres do I read most?",
                "Show my reading statistics",
                "What's the total value of my library?"
        );
    }
}