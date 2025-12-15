package com.library.library_management.dto.request;

import com.library.library_management.entity.enums.Genre;
import com.library.library_management.entity.enums.ReadingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookSearchRequest {

    // Full-text search query (searches title, author, description, ISBN)
    private String query;

    // Individual field searches (more specific)
    private String title;
    private String author;
    private String isbn;

    // Filters
    private Genre genre;
    private ReadingStatus status;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer minYear;
    private Integer maxYear;
    private Integer minPages;
    private Integer maxPages;

    // Pagination
    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 10;

    // Sorting
    @Builder.Default
    private String sortBy = "relevance"; // relevance, title, author, price, createdAt, publicationYear

    @Builder.Default
    private String sortDir = "desc"; // asc, desc

    /**
     * Check if any search criteria is specified
     */
    public boolean hasSearchCriteria() {
        return (query != null && !query.isBlank()) ||
               (title != null && !title.isBlank()) ||
               (author != null && !author.isBlank()) ||
               (isbn != null && !isbn.isBlank()) ||
               genre != null ||
               status != null ||
               minPrice != null ||
               maxPrice != null ||
               minYear != null ||
               maxYear != null ||
               minPages != null ||
               maxPages != null;
    }

    /**
     * Check if full-text search is requested
     */
    public boolean hasFullTextQuery() {
        return query != null && !query.isBlank();
    }
}