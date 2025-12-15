package com.library.library_management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookSearchResponse {

    private List<BookResponse> books;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;

    // Search metadata
    private String query;
    private long searchTimeMs;
    private Map<String, Long> facets; // Optional: genre counts, status counts, etc.

    // Suggestions (when no results found)
    private List<String> suggestions;
}