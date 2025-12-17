package com.library.library_management.controller;

import com.library.library_management.dto.response.BookResponse;
import com.library.library_management.dto.response.PagedResponse;
import com.library.library_management.security.CustomUserDetails;
import com.library.library_management.service.GlobalLibraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/library")
@RequiredArgsConstructor
public class LibraryController {

    private final GlobalLibraryService globalLibraryService;

    @GetMapping("/books")
    public PagedResponse<BookResponse> listSharedBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = Sort.by(sortBy);
        sort = "asc".equalsIgnoreCase(sortDir) ? sort.ascending() : sort.descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return globalLibraryService.listSharedBooks(pageable);
    }

    @PostMapping("/books/{id}/claim")
    public BookResponse claimSharedBook(
            @PathVariable("id") Long bookId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return globalLibraryService.claimBook(bookId, userDetails.getId());
    }
}
