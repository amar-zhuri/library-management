package com.library.library_management.controller;

import com.library.library_management.dto.request.BookRequest;
import com.library.library_management.dto.response.BookResponse;
import com.library.library_management.dto.response.PagedResponse;
import com.library.library_management.entity.Book;
import com.library.library_management.entity.enums.Genre;
import com.library.library_management.entity.enums.ReadingStatus;
import com.library.library_management.security.CustomUserDetails;
import com.library.library_management.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.hibernate.query.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.Map;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Slf4j
public class BookController {

    private final BookService bookService;

    /**
     * Create a new book
     * POST /api/books
     */
    @PostMapping
    public ResponseEntity<BookResponse> createBook(
            @Valid @RequestBody BookRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("POST /api/books - Creating book for user {}", userDetails.getId());
        BookResponse response = bookService.createBook(request, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all books for the authenticated user with optional filtering
     * GET /api/books?genre=FANTASY&status=READING&search=hobbit&page=0&size=10
     */
    @GetMapping
    public ResponseEntity<PagedResponse<BookResponse>> getBooks(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Genre genre,
            @RequestParam(required = false) ReadingStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("GET /api/books - Fetching books for user {}", userDetails.getId());

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        PagedResponse<BookResponse> response = bookService.getUserBooks(
                userDetails.getId(), genre, status, search, pageable);

        return ResponseEntity.ok(response);
    }

    /**
     * Get a single book by ID
     * GET /api/books/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getBook(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("GET /api/books/{} - Fetching book for user {}", id, userDetails.getId());
        BookResponse response = bookService.getBookById(id, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Update a book
     * PUT /api/books/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("PUT /api/books/{} - Updating book for user {}", id, userDetails.getId());
        BookResponse response = bookService.updateBook(id, request, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a book
     * DELETE /api/books/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("DELETE /api/books/{} - Deleting book for user {}", id, userDetails.getId());
        bookService.deleteBook(id, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Get book statistics for the authenticated user
     * GET /api/books/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getBookStats(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("GET /api/books/stats - Fetching stats for user {}", userDetails.getId());

        Map<String, Long> stats = Map.of(
                "total", bookService.getBookCount(userDetails.getId()),
                "toRead", bookService.getBookCountByStatus(userDetails.getId(), ReadingStatus.TO_READ),
                "reading", bookService.getBookCountByStatus(userDetails.getId(), ReadingStatus.READING),
                "completed", bookService.getBookCountByStatus(userDetails.getId(), ReadingStatus.COMPLETED)
        );

        return ResponseEntity.ok(stats);
    }

    /**
     * Quick search books (simple text search)
     * GET /api/books/quick-search?q=tolkien
     */
    @GetMapping("/quick-search")
    public ResponseEntity<PagedResponse<BookResponse>> quickSearch(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("GET /api/books/quick-search - q='{}' for user {}", q, userDetails.getId());

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PagedResponse<BookResponse> response = bookService.quickSearch(q, userDetails.getId(), pageable);

        return ResponseEntity.ok(response);
    }

}