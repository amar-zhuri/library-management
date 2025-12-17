package com.library.library_management.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.library.library_management.dto.request.BookRequest;
import com.library.library_management.dto.response.BookResponse;
import com.library.library_management.dto.response.PagedResponse;
import com.library.library_management.entity.Book;
import com.library.library_management.entity.User;
import com.library.library_management.entity.enums.Role;
import com.library.library_management.entity.enums.Genre;
import com.library.library_management.entity.enums.ReadingStatus;
import com.library.library_management.exception.ResourceNotFoundException;
import com.library.library_management.exception.UnauthorizedException;
import com.library.library_management.repository.BookRepository;
import com.library.library_management.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
   import com.library.library_management.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookService {
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
 


    @Transactional
    public BookResponse createBook(BookRequest request, Long userId)
    {
        log.info("Creating book '{}' for user {}", request.getTitle(), userId);
         User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    
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
                .user(user)
                .build();
        Book savedBook = bookRepository.save(book);
        log.info("Book created with id {}", savedBook.getId());

        return BookResponse.fromEntity(savedBook);
    }
    /**
     * Get a single book by ID (only if owned by user)
     */
    public BookResponse getBookById(Long bookId, Long userId) {
        log.debug("Fetching book {} for user {}", bookId, userId);

        Book book = bookRepository.findByIdAndUserId(bookId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", bookId));

        return BookResponse.fromEntity(book);
    }

    public PagedResponse<BookResponse> getUserBooks(
            Long userId,
            Genre genre,
            ReadingStatus status,
            String search,
            Pageable pageable) {

        log.debug("Fetching books for user {} with filters - genre: {}, status: {}, search: {}",
                userId, genre, status, search);

        Page<Book> bookPage;

        // Apply filters based on what's provided
        if (search != null && !search.isBlank()) {
            // Search by title (could extend to author as well)
            bookPage = bookRepository.findByUserIdAndTitleContainingIgnoreCase(userId, search, pageable);
        } else if (genre != null && status != null) {
            bookPage = bookRepository.findByUserIdAndGenreAndStatus(userId, genre, status, pageable);
        } else if (genre != null) {
            bookPage = bookRepository.findByUserIdAndGenre(userId, genre, pageable);
        } else if (status != null) {
            bookPage = bookRepository.findByUserIdAndStatus(userId, status, pageable);
        } else {
            bookPage = bookRepository.findByUserId(userId, pageable);
        }

        // Convert entities to DTOs
        Page<BookResponse> responsePage = bookPage.map(BookResponse::fromEntity);

        return PagedResponse.fromPage(responsePage);
    }
    /**
     * Update an existing book
     */
    @Transactional
    public BookResponse updateBook(Long bookId, BookRequest request, Long userId) {
        log.info("Updating book {} for user {}", bookId, userId);

        Book book = bookRepository.findByIdAndUserId(bookId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", bookId));

        // Update fields
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setGenre(request.getGenre());
        if (request.getStatus() != null) {
            book.setStatus(request.getStatus());
        }
        book.setPrice(request.getPrice());
        book.setDescription(request.getDescription());
        book.setIsbn(request.getIsbn());
        book.setPageCount(request.getPageCount());
        book.setPublicationYear(request.getPublicationYear());

        Book updatedBook = bookRepository.save(book);
        log.info("Book {} updated successfully", bookId);

        return BookResponse.fromEntity(updatedBook);
    }

    /**
     * Delete a book
     */
    @Transactional
    public void deleteBook(Long bookId, Long userId) {
        log.info("Deleting book {} for user {}", bookId, userId);

        Book book = bookRepository.findByIdAndUserId(bookId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", bookId));

        if (book.getUser().getRole() == Role.ADMIN) {
            throw new UnauthorizedException("System library books cannot be deleted from user workspace. Use admin tools.");
        }

        bookRepository.delete(book);
        log.info("Book {} deleted successfully", bookId);
    }

    /**
     * Get book count for a user
     */
    public long getBookCount(Long userId) {
        return bookRepository.countByUserId(userId);
    }

    /**
     * Get book count by status for a user
     */
    public long getBookCountByStatus(Long userId, ReadingStatus status) {
        return bookRepository.countByUserIdAndStatus(userId, status);
    }
    /**
     * Quick search books by title or author
     */
    public PagedResponse<BookResponse> quickSearch(String query, Long userId, Pageable pageable) {
        log.info("Quick search for user {}: '{}'", userId, query);

        Page<Book> bookPage = bookRepository
                .findByUserIdAndTitleContainingIgnoreCaseOrUserIdAndAuthorContainingIgnoreCase(
                        userId, query, userId, query, pageable);

        Page<BookResponse> responsePage = bookPage.map(BookResponse::fromEntity);
        return PagedResponse.fromPage(responsePage);
    }
}
