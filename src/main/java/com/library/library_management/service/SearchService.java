package com.library.library_management.service;

import com.library.library_management.dto.request.BookSearchRequest;
import com.library.library_management.dto.response.BookResponse;
import com.library.library_management.dto.response.BookSearchResponse;
import com.library.library_management.entity.Book;
import com.library.library_management.entity.enums.Genre;
import com.library.library_management.repository.BookRepository;
import com.library.library_management.repository.specification.BookSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SearchService {

    private final BookRepository bookRepository;

    /**
     * Search books for a specific user
     */
    public BookSearchResponse searchBooks(BookSearchRequest request, Long userId) {
        long startTime = System.currentTimeMillis();
        log.info("Searching books for user {} with query: '{}'", userId, request.getQuery());

        // Build specification
        Specification<Book> spec = BookSpecification.buildSpecification(request, userId);

        // Build pageable with sorting
        Pageable pageable = buildPageable(request);

        // Execute search
        Page<Book> bookPage = bookRepository.findAll(spec, pageable);

        // Convert to response
        List<BookResponse> books = bookPage.getContent().stream()
                .map(BookResponse::fromEntity)
                .collect(Collectors.toList());

        // Build facets (genre and status counts)
        Map<String, Long> facets = buildFacets(userId, request.getQuery());

        // Build suggestions if no results
        List<String> suggestions = books.isEmpty() ? buildSuggestions(userId, request) : null;

        long searchTime = System.currentTimeMillis() - startTime;
        log.info("Search completed in {}ms, found {} results", searchTime, bookPage.getTotalElements());

        return BookSearchResponse.builder()
                .books(books)
                .page(bookPage.getNumber())
                .size(bookPage.getSize())
                .totalElements(bookPage.getTotalElements())
                .totalPages(bookPage.getTotalPages())
                .first(bookPage.isFirst())
                .last(bookPage.isLast())
                .query(request.getQuery())
                .searchTimeMs(searchTime)
                .facets(facets)
                .suggestions(suggestions)
                .build();
    }

    /**
     * Search all books (Admin)
     */
    public BookSearchResponse searchAllBooks(BookSearchRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("Admin searching all books with query: '{}'", request.getQuery());

        // Build specification without user filter
        Specification<Book> spec = BookSpecification.buildAdminSpecification(request);

        // Build pageable with sorting
        Pageable pageable = buildPageable(request);

        // Execute search
        Page<Book> bookPage = bookRepository.findAll(spec, pageable);

        // Convert to response with owner info
        List<BookResponse> books = bookPage.getContent().stream()
                .map(BookResponse::fromEntityWithOwner)
                .collect(Collectors.toList());

        long searchTime = System.currentTimeMillis() - startTime;

        return BookSearchResponse.builder()
                .books(books)
                .page(bookPage.getNumber())
                .size(bookPage.getSize())
                .totalElements(bookPage.getTotalElements())
                .totalPages(bookPage.getTotalPages())
                .first(bookPage.isFirst())
                .last(bookPage.isLast())
                .query(request.getQuery())
                .searchTimeMs(searchTime)
                .build();
    }

    /**
     * Get search suggestions (autocomplete)
     */
    public SearchSuggestions getSuggestions(String query, Long userId) {
        log.debug("Getting suggestions for query: '{}' user: {}", query, userId);

        if (query == null || query.length() < 2) {
            return SearchSuggestions.empty();
        }

        Pageable limit = PageRequest.of(0, 5);

        List<String> titles = bookRepository.findDistinctTitlesByUserIdAndQuery(userId, query, limit);
        List<String> authors = bookRepository.findDistinctAuthorsByUserIdAndQuery(userId, query, limit);

        return SearchSuggestions.builder()
                .titles(titles)
                .authors(authors)
                .build();
    }

    /**
     * Get available filters for a user
     */
    public SearchFilters getAvailableFilters(Long userId) {
        log.debug("Getting available filters for user: {}", userId);

        // Get distinct genres
        List<Genre> genres = bookRepository.findDistinctGenresByUserId(userId);

        // Get distinct authors
        List<String> authors = bookRepository.findDistinctAuthorsByUserId(userId);

        // Get price range
        BigDecimal minPrice = null, maxPrice = null;
        List<Object[]> priceRange = bookRepository.findPriceRangeByUserId(userId);
        if (!priceRange.isEmpty() && priceRange.get(0)[0] != null) {
            minPrice = (BigDecimal) priceRange.get(0)[0];
            maxPrice = (BigDecimal) priceRange.get(0)[1];
        }

        // Get year range
        Integer minYear = null, maxYear = null;
        List<Object[]> yearRange = bookRepository.findYearRangeByUserId(userId);
        if (!yearRange.isEmpty() && yearRange.get(0)[0] != null) {
            minYear = (Integer) yearRange.get(0)[0];
            maxYear = (Integer) yearRange.get(0)[1];
        }

        return SearchFilters.builder()
                .genres(genres)
                .authors(authors)
                .statuses(List.of(com.library.library_management.entity.enums.ReadingStatus.values()))
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .minYear(minYear)
                .maxYear(maxYear)
                .build();
    }

    /**
     * Build pageable with proper sorting
     */
    private Pageable buildPageable(BookSearchRequest request) {
        Sort sort;

        switch (request.getSortBy().toLowerCase()) {
            case "title" -> sort = Sort.by("title");
            case "author" -> sort = Sort.by("author");
            case "price" -> sort = Sort.by("price");
            case "publicationyear", "year" -> sort = Sort.by("publicationYear");
            case "createdat", "date", "added" -> sort = Sort.by("createdAt");
            case "pagecount", "pages" -> sort = Sort.by("pageCount");
            case "relevance" -> {
                // For relevance, we use createdAt as default (could be enhanced with scoring)
                sort = Sort.by("createdAt");
            }
            default -> sort = Sort.by("createdAt");
        }

        if ("asc".equalsIgnoreCase(request.getSortDir())) {
            sort = sort.ascending();
        } else {
            sort = sort.descending();
        }

        return PageRequest.of(request.getPage(), request.getSize(), sort);
    }

    /**
     * Build facets for faceted search
     */
    private Map<String, Long> buildFacets(Long userId, String query) {
        Map<String, Long> facets = new LinkedHashMap<>();
        String safeQuery = query == null ? "" : query;

        // Genre facets
        List<Object[]> genreCounts = bookRepository.countByGenreWithQuery(userId, safeQuery);
        for (Object[] row : genreCounts) {
            facets.put("genre:" + row[0].toString(), (Long) row[1]);
        }

        // Status facets
        List<Object[]> statusCounts = bookRepository.countByStatusWithQuery(userId, safeQuery);
        for (Object[] row : statusCounts) {
            facets.put("status:" + row[0].toString(), (Long) row[1]);
        }

        return facets;
    }

    /**
     * Build search suggestions when no results found
     */
    private List<String> buildSuggestions(Long userId, BookSearchRequest request) {
        List<String> suggestions = new ArrayList<>();

        if (request.hasFullTextQuery()) {
            suggestions.add("Try using fewer keywords");
            suggestions.add("Check spelling of your search terms");
            suggestions.add("Try searching by author or title separately");
        }

        if (request.getGenre() != null || request.getStatus() != null) {
            suggestions.add("Try removing some filters");
        }

        if (request.getMinPrice() != null || request.getMaxPrice() != null) {
            suggestions.add("Try widening the price range");
        }

        // Add some popular authors as suggestions
        List<Object[]> topAuthors = bookRepository.findTopAuthorsForUser(userId, PageRequest.of(0, 3));
        if (!topAuthors.isEmpty()) {
            suggestions.add("Try searching for: " + topAuthors.stream()
                    .map(row -> (String) row[0])
                    .collect(Collectors.joining(", ")));
        }

        return suggestions;
    }

    // ========== Inner Classes for Suggestions and Filters ==========

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SearchSuggestions {
        private List<String> titles;
        private List<String> authors;

        public static SearchSuggestions empty() {
            return SearchSuggestions.builder()
                    .titles(List.of())
                    .authors(List.of())
                    .build();
        }
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SearchFilters {
        private List<Genre> genres;
        private List<String> authors;
        private List<com.library.library_management.entity.enums.ReadingStatus> statuses;
        private BigDecimal minPrice;
        private BigDecimal maxPrice;
        private Integer minYear;
        private Integer maxYear;
    }
}
