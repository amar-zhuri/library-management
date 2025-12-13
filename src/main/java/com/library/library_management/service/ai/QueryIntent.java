package com.library.library_management.service.ai;

import com.library.library_management.entity.enums.QueryType;
import com.library.library_management.entity.enums.Genre;
import com.library.library_management.entity.enums.ReadingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryIntent {
    
    private QueryType queryType;
    private Integer limit;           // For "top N" queries
    private Genre genre;             // For genre-specific queries
    private ReadingStatus status;    // For status-specific queries
    private String authorName;       // For author-specific queries
    private String searchTerm;       // For search queries
    private Double confidence;       // How confident we are in the parse (0.0 - 1.0)
    private String originalQuestion; // The original question text
    
    public static QueryIntent unknown(String question) {
        return QueryIntent.builder()
                .queryType(QueryType.UNKNOWN)
                .originalQuestion(question)
                .confidence(0.0)
                .build();
    }
    
    public boolean isRecognized() {
        return queryType != QueryType.UNKNOWN;
    }
}