package com.library.library_management.controller;

import com.library.library_management.dto.request.BookSearchRequest;
import com.library.library_management.dto.response.BookSearchResponse;
import com.library.library_management.entity.enums.Genre;
import com.library.library_management.entity.enums.ReadingStatus;
import com.library.library_management.security.CustomUserDetails;
import com.library.library_management.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/books/search")
@RequiredArgsConstructor
@Slf4j
public class SearchController {

    private final SearchService searchService;

    /**
     * Search user's books with filters
     * GET /api/books/search?q=tolkien&genre=FANTASY&minPrice=10&maxPrice=50
     */
    @GetMapping
    public ResponseEntity<BookSearchResponse> searchBooks(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String isbn,
            @RequestParam(required = false) Genre genre,
            @RequestParam(required = false) ReadingStatus status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer minYear,
            @RequestParam(required = false) Integer maxYear,
            @RequestParam(required = false) Integer minPages,
            @RequestParam(required = false) Integer maxPages,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "relevance") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("GET /api/books/search - User {} searching: q='{}', genre={}, status={}",
                userDetails.getId(), q, genre, status);

        BookSearchRequest request = BookSearchRequest.builder()
                .query(q)
                .title(title)
                .author(author)
                .isbn(isbn)
                .genre(genre)
                .status(status)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .minYear(minYear)
                .maxYear(maxYear)
                .minPages(minPages)
                .maxPages(maxPages)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDir(sortDir)
                .build();

        BookSearchResponse response = searchService.searchBooks(request, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Get search suggestions (autocomplete)
     * GET /api/books/search/suggestions?q=tol
     */
    @GetMapping("/suggestions")
    public ResponseEntity<SearchService.SearchSuggestions> getSuggestions(
            @RequestParam String q,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.debug("GET /api/books/search/suggestions - q='{}'", q);

        SearchService.SearchSuggestions suggestions = searchService.getSuggestions(q, userDetails.getId());
        return ResponseEntity.ok(suggestions);
    }

    /**
     * Get available filters for the user
     * GET /api/books/search/filters
     */
    @GetMapping("/filters")
    public ResponseEntity<SearchService.SearchFilters> getFilters(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("GET /api/books/search/filters for user {}", userDetails.getId());

        SearchService.SearchFilters filters = searchService.getAvailableFilters(userDetails.getId());
        return ResponseEntity.ok(filters);
    }

    /**
     * Admin: Search all books in the system
     * GET /api/books/search/all?q=tolkien
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookSearchResponse> searchAllBooks(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String isbn,
            @RequestParam(required = false) Genre genre,
            @RequestParam(required = false) ReadingStatus status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer minYear,
            @RequestParam(required = false) Integer maxYear,
            @RequestParam(required = false) Integer minPages,
            @RequestParam(required = false) Integer maxPages,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("GET /api/books/search/all - Admin searching: q='{}'", q);

        BookSearchRequest request = BookSearchRequest.builder()
                .query(q)
                .title(title)
                .author(author)
                .isbn(isbn)
                .genre(genre)
                .status(status)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .minYear(minYear)
                .maxYear(maxYear)
                .minPages(minPages)
                .maxPages(maxPages)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDir(sortDir)
                .build();

        BookSearchResponse response = searchService.searchAllBooks(request);
        return ResponseEntity.ok(response);
    }
}
