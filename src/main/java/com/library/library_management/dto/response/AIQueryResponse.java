package com.library.library_management.dto.response;

import com.library.library_management.entity.enums.QueryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIQueryResponse {

    private String question;
    private String answer;
    private QueryType queryType;
    private Object data;
    private boolean recognizedQuery;
    private String processingMethod; // "RULE_BASED" or "LLM"
    private Double confidence;
    private long executionTimeMs;
    private List<String> suggestions; // Shown when query not recognized
}