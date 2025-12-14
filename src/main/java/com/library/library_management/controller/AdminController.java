package com.library.library_management.controller;

import com.library.library_management.dto.response.BookResponse;
import com.library.library_management.dto.response.PagedResponse;
import com.library.library_management.dto.response.StatsResponse;
import com.library.library_management.dto.response.UserDetailResponse;
import com.library.library_management.dto.response.UserResponse;
import com.library.library_management.security.CustomUserDetails;
import com.library.library_management.service.AdminService;
import com.library.library_management.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.library.library_management.dto.request.BookRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;



@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final AdminService adminService;

    // ==================== USER MANAGEMENT ====================

    /**
     * Get all users
     * GET /api/admin/users?page=0&size=10
     */
    @GetMapping("/users")
    public ResponseEntity<PagedResponse<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("GET /api/admin/users - Fetching all users");

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        PagedResponse<UserResponse> response = userService.getAllUsers(pageable);

        return ResponseEntity.ok(response);
    }

    /**
     * Get user details with their books
     * GET /api/admin/users/{id}
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<UserDetailResponse> getUserById(@PathVariable Long id) {
        log.info("GET /api/admin/users/{} - Fetching user details", id);

        UserDetailResponse response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a user and all their books
     * DELETE /api/admin/users/{id}
     */
    // Delete all boooks behet  ne user.java cascadee = CascadeTyepe.all
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails adminDetails) {

        log.info("DELETE /api/admin/users/{} - Admin {} deleting user", id, adminDetails.getId());

        userService.deleteUser(id, adminDetails.getId());
        return ResponseEntity.noContent().build();
    }

    // ==================== BOOK MANAGEMENT ====================

    /**
     * Get all books in the system
     * GET /api/admin/books?page=0&size=10
     */
    @GetMapping("/books")
    public ResponseEntity<PagedResponse<BookResponse>> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("GET /api/admin/books - Fetching all books");

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        PagedResponse<BookResponse> response = adminService.getAllBooks(pageable);

        return ResponseEntity.ok(response);
    }

    /**
     * Delete any book
     * DELETE /api/admin/books/{id}
     */
    @DeleteMapping("/books/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        log.info("DELETE /api/admin/books/{} - Deleting book", id);

        adminService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Add a new book to the system (triggers new book notifications)
     * POST /api/admin/books
     */
    @PostMapping("/books")
    public ResponseEntity<BookResponse> addBook(
            @Valid @RequestBody BookRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("POST /api/admin/books - Admin adding book: {}", request.getTitle());
        BookResponse response = adminService.addBookAsAdmin(request, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    // ==================== STATISTICS ====================

    /**
     * Get system-wide statistics
     * GET /api/admin/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<StatsResponse> getStatistics() {
        log.info("GET /api/admin/stats - Generating statistics");

        StatsResponse response = adminService.getStatistics();
        return ResponseEntity.ok(response);
    }
}