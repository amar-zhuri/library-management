package com.library.library_management.controller;

import com.library.library_management.dto.request.BookRequest;
import com.library.library_management.dto.response.BookResponse;
import com.library.library_management.dto.response.PagedResponse;
import com.library.library_management.entity.enums.Genre;
import com.library.library_management.entity.enums.ReadingStatus;
import com.library.library_management.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Slf4j
public class BookController {

    private final BookService bookService;

    // TODO: In Phase 4, we'll get userId from the authenticated user (JWT)
    // For now, we'll accept it as a request header for testing
    private static final String USER_ID_HEADER = "X-User-Id";

    /**
     * Create a new book
     * POST /api/books
     */
    @PostMapping
    public ResponseEntity<BookResponse> createBook(
            @Valid @RequestBody BookRequest request,
            @RequestHeader(USER_ID_HEADER) Long userId) {

        log.info("POST /api/books - Creating book for user {}", userId);
        BookResponse response = bookService.createBook(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all books for the user with optional filtering
     * GET /api/books?genre=FANTASY&status=READING&search=hobbit&page=0&size=10&sort=title,asc
     */
    @GetMapping
    public ResponseEntity<PagedResponse<BookResponse>> getBooks(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @RequestParam(required = false) Genre genre,
            @RequestParam(required = false) ReadingStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("GET /api/books - Fetching books for user {}", userId);

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        PagedResponse<BookResponse> response = bookService.getUserBooks(
                userId, genre, status, search, pageable);

        return ResponseEntity.ok(response);
    }

    /**
     * Get a single book by ID
     * GET /api/books/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getBook(
            @PathVariable Long id,
            @RequestHeader(USER_ID_HEADER) Long userId) {

        log.info("GET /api/books/{} - Fetching book for user {}", id, userId);
        BookResponse response = bookService.getBookById(id, userId);
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
            @RequestHeader(USER_ID_HEADER) Long userId) {

        log.info("PUT /api/books/{} - Updating book for user {}", id, userId);
        BookResponse response = bookService.updateBook(id, request, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a book
     * DELETE /api/books/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(
            @PathVariable Long id,
            @RequestHeader(USER_ID_HEADER) Long userId) {

        log.info("DELETE /api/books/{} - Deleting book for user {}", id, userId);
        bookService.deleteBook(id, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get book statistics for the user
     * GET /api/books/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getBookStats(@RequestHeader(USER_ID_HEADER) Long userId) {

        log.info("GET /api/books/stats - Fetching stats for user {}", userId);

        var stats = java.util.Map.of(
                "total", bookService.getBookCount(userId),
                "toRead", bookService.getBookCountByStatus(userId, ReadingStatus.TO_READ),
                "reading", bookService.getBookCountByStatus(userId, ReadingStatus.READING),
                "completed", bookService.getBookCountByStatus(userId, ReadingStatus.COMPLETED)
        );

        return ResponseEntity.ok(stats);
    }
}