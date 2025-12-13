package com.library.library_management.service.ai;

import com.library.library_management.dto.response.AIQueryResponse;

public interface LLMService {

    /**
     * Process a natural language query using LLM
     */
    AIQueryResponse processQuery(String question, Long userId);

    /**
     * Generate insights using LLM
     */
    String generateInsight(String context);

    /**
     * Check if the LLM service is available
     */
    boolean isAvailable();
}