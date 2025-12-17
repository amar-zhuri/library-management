package com.library.library_management.service;

import com.library.library_management.dto.request.BookRequest;
import com.library.library_management.dto.response.BookResponse;
import com.library.library_management.dto.response.PagedResponse;
import com.library.library_management.entity.Book;
import com.library.library_management.entity.enums.ReadingStatus;
import com.library.library_management.entity.enums.Role;
import com.library.library_management.exception.ResourceNotFoundException;
import com.library.library_management.repository.BookRepository;
import com.library.library_management.repository.GlobalBookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.domain.Specification;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GlobalLibraryService {

    private final GlobalBookRepository globalBookRepository;
    private final BookService bookService;
    private final BookRepository bookRepository;

    /**
     * List admin-owned books (shared library).
     */
    public PagedResponse<BookResponse> listSharedBooks(Pageable pageable) {
        Page<Book> page = globalBookRepository.findAllByOwnerRole(Role.ADMIN, pageable);
        Page<BookResponse> dtoPage = page.map(BookResponse::fromEntityWithOwner);
        return PagedResponse.fromPage(dtoPage);
    }

    /**
     * Copy an admin book into a user's personal library.
     */
    @Transactional
    public BookResponse claimBook(Long adminBookId, Long userId) {
        log.info("User {} claiming shared book {}", userId, adminBookId);

        Book template = globalBookRepository.findByIdAndOwnerRole(adminBookId, Role.ADMIN)
                .orElseThrow(() -> new ResourceNotFoundException("Book", adminBookId));

        // Prevent duplicate (same title + author) in user's own library
        Specification<Book> duplicateSpec = (root, query, cb) -> cb.and(
                cb.equal(root.get("user").get("id"), userId),
                cb.equal(cb.lower(root.get("title")), template.getTitle().toLowerCase()),
                cb.equal(cb.lower(root.get("author")), template.getAuthor().toLowerCase())
        );

        if (bookRepository.exists(duplicateSpec)) {
            throw new IllegalArgumentException("You already have this book in your library");
        }

        BookRequest request = BookRequest.builder()
                .title(template.getTitle())
                .author(template.getAuthor())
                .genre(template.getGenre())
                // Always start claimed books as TO_READ so users can track their own progress
                .status(ReadingStatus.TO_READ)
                .price(template.getPrice())
                .description(template.getDescription())
                .isbn(template.getIsbn())
                .pageCount(template.getPageCount())
                .publicationYear(template.getPublicationYear())
                .build();

        return bookService.createBook(request, userId);
    }
}
