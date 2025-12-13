package com.library.library_management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsightResponse {

    private List<String> insights;
    private String generatedBy; // "RULE_BASED" or "LLM"
    private long generatedAtMs;
}