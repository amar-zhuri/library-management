package com.library.library_management.service;

import com.library.library_management.dto.response.BookResponse;
import com.library.library_management.dto.response.PagedResponse;
import com.library.library_management.dto.response.StatsResponse;
import com.library.library_management.entity.Book;
import com.library.library_management.entity.User;
import com.library.library_management.exception.ResourceNotFoundException;
import com.library.library_management.repository.BookRepository;
import com.library.library_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.library.library_management.dto.request.BookRequest;
import com.library.library_management.entity.enums.ReadingStatus;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final BookEventService bookEventService;

    /**
     * Get all books in the system (Admin only)
     */
    /**
 * Get all books in the system (Admin only)
 */
public PagedResponse<BookResponse> getAllBooks(Pageable pageable) {
    log.info("Admin fetching all books");

    Page<Book> bookPage = bookRepository.findAll(pageable);
    Page<BookResponse> responsePage = bookPage.map(BookResponse::fromEntityWithOwner);

    return PagedResponse.fromPage(responsePage);
}

    /**
     * Delete any book by ID (Admin only)
     */
    @Transactional
    public void deleteBook(Long bookId) {
        log.info("Admin deleting book {}", bookId);

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", bookId));

        bookRepository.delete(book);
        log.info("Book {} deleted successfully", bookId);
    }

    /**
     * Get system-wide statistics (Admin only)
     */
    public StatsResponse getStatistics() {
        log.info("Generating system statistics");

        // Total counts
        long totalUsers = userRepository.count();
        long totalBooks = bookRepository.count();

        // Books by genre
        Map<String, Long> booksByGenre = new LinkedHashMap<>();
        List<Object[]> genreStats = bookRepository.countBooksByGenre();
        for (Object[] row : genreStats) {
            booksByGenre.put(row[0].toString(), (Long) row[1]);
        }

        // Books by status
        Map<String, Long> booksByStatus = new LinkedHashMap<>();
        List<Object[]> statusStats = bookRepository.countBooksByStatus();
        for (Object[] row : statusStats) {
            booksByStatus.put(row[0].toString(), (Long) row[1]);
        }

        // Top readers
        List<StatsResponse.TopReaderDto> topReaders = bookRepository.countBooksPerUser()
                .stream()
                .limit(5)
                .map(row -> StatsResponse.TopReaderDto.builder()
                        .userId((Long) row[0])
                        .userName((String) row[1])
                        .bookCount((Long) row[2])
                        .build())
                .collect(Collectors.toList());

        // Popular books
        List<StatsResponse.PopularBookDto> popularBooks = bookRepository
                .findMostPopularBooks(PageRequest.of(0, 5))
                .stream()
                .map(row -> StatsResponse.PopularBookDto.builder()
                        .title((String) row[0])
                        .author((String) row[1])
                        .count((Long) row[2])
                        .build())
                .collect(Collectors.toList());

        // Top authors
        List<StatsResponse.TopAuthorDto> topAuthors = bookRepository
                .findTopAuthors(PageRequest.of(0, 5))
                .stream()
                .map(row -> StatsResponse.TopAuthorDto.builder()
                        .author((String) row[0])
                        .bookCount((Long) row[1])
                        .build())
                .collect(Collectors.toList());

        return StatsResponse.builder()
                .totalUsers(totalUsers)
                .totalBooks(totalBooks)
                .booksByGenre(booksByGenre)
                .booksByStatus(booksByStatus)
                .topReaders(topReaders)
                .popularBooks(popularBooks)
                .topAuthors(topAuthors)
                .build();
    }
    /**
     * Add a book as admin (triggers notifications)
     */
    @Transactional
    public BookResponse addBookAsAdmin(BookRequest request, Long adminId) {
        log.info("Admin {} adding book to system", adminId);

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("User", adminId));

        Book book = Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .genre(request.getGenre())
                .status(request.getStatus() != null ? request.getStatus() : ReadingStatus.TO_READ)
                .price(request.getPrice())
                .description(request.getDescription())
                .isbn(request.getIsbn())
                .pageCount(request.getPageCount())
                .publicationYear(request.getPublicationYear())
                .user(admin)
                .build();

        Book savedBook = bookRepository.save(book);
        log.info("Admin added book with id {}", savedBook.getId());

        // Trigger notifications asynchronously
        bookEventService.onBookAddedByAdmin(savedBook, admin);

        return BookResponse.fromEntityWithOwner(savedBook);
    }
}