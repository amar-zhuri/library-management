package com.library.library_management.service;

import com.library.library_management.dto.request.BookRequest;
import com.library.library_management.dto.response.BookResponse;
import com.library.library_management.dto.response.PagedResponse;
import com.library.library_management.entity.Book;
import com.library.library_management.entity.User;
import com.library.library_management.entity.enums.Genre;
import com.library.library_management.entity.enums.ReadingStatus;
import com.library.library_management.entity.enums.Role;
import com.library.library_management.exception.ResourceNotFoundException;
import com.library.library_management.repository.BookRepository;
import com.library.library_management.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookService Unit Tests")
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BookService bookService;

    private User testUser;
    private Book testBook;
    private BookRequest bookRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .password("encoded_password")
                .role(Role.USER)
                .emailVerified(true)
                .build();

        testBook = Book.builder()
                .id(1L)
                .title("Clean Code")
                .author("Robert C. Martin")
                .genre(Genre.TECHNOLOGY)
                .status(ReadingStatus.READING)
                .price(new BigDecimal("29.99"))
                .description("A handbook of agile software craftsmanship")
                .isbn("978-0132350884")
                .pageCount(464)
                .publicationYear(2008)
                .user(testUser)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        bookRequest = BookRequest.builder()
                .title("Clean Code")
                .author("Robert C. Martin")
                .genre(Genre.TECHNOLOGY)
                .status(ReadingStatus.READING)
                .price(new BigDecimal("29.99"))
                .description("A handbook of agile software craftsmanship")
                .isbn("978-0132350884")
                .pageCount(464)
                .publicationYear(2008)
                .build();
    }

    @Nested
    @DisplayName("createBook tests")
    class CreateBookTests {

        @Test
        @DisplayName("Should create book successfully")
        void shouldCreateBookSuccessfully() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(bookRepository.save(any(Book.class))).thenReturn(testBook);

            // When
            BookResponse response = bookService.createBook(bookRequest, 1L);

            // Then
            assertNotNull(response);
            assertEquals("Clean Code", response.getTitle());
            assertEquals("Robert C. Martin", response.getAuthor());
            assertEquals(Genre.TECHNOLOGY, response.getGenre());
            verify(bookRepository, times(1)).save(any(Book.class));
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ResourceNotFoundException.class,
                    () -> bookService.createBook(bookRequest, 999L));
            verify(bookRepository, never()).save(any(Book.class));
        }

        @Test
        @DisplayName("Should set default status to TO_READ when not provided")
        void shouldSetDefaultStatusWhenNotProvided() {
            // Given
            BookRequest requestWithoutStatus = BookRequest.builder()
                    .title("Test Book")
                    .author("Test Author")
                    .genre(Genre.FICTION)
                    .build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
                Book savedBook = invocation.getArgument(0);
                savedBook.setId(1L);
                savedBook.setCreatedAt(LocalDateTime.now());
                savedBook.setUpdatedAt(LocalDateTime.now());
                return savedBook;
            });

            // When
            BookResponse response = bookService.createBook(requestWithoutStatus, 1L);

            // Then
            assertNotNull(response);
            assertEquals(ReadingStatus.TO_READ, response.getStatus());
        }
    }

    @Nested
    @DisplayName("getBookById tests")
    class GetBookByIdTests {

        @Test
        @DisplayName("Should return book when found")
        void shouldReturnBookWhenFound() {
            // Given
            when(bookRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testBook));

            // When
            BookResponse response = bookService.getBookById(1L, 1L);

            // Then
            assertNotNull(response);
            assertEquals("Clean Code", response.getTitle());
            assertEquals(1L, response.getId());
        }

        @Test
        @DisplayName("Should throw exception when book not found")
        void shouldThrowExceptionWhenBookNotFound() {
            // Given
            when(bookRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ResourceNotFoundException.class,
                    () -> bookService.getBookById(999L, 1L));
        }
    }

    @Nested
    @DisplayName("getUserBooks tests")
    class GetUserBooksTests {

        @Test
        @DisplayName("Should return paginated books for user")
        void shouldReturnPaginatedBooksForUser() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Book> bookPage = new PageImpl<>(List.of(testBook), pageable, 1);
            when(bookRepository.findByUserId(1L, pageable)).thenReturn(bookPage);

            // When
            PagedResponse<BookResponse> response = bookService.getUserBooks(1L, null, null, null, pageable);

            // Then
            assertNotNull(response);
            assertEquals(1, response.getContent().size());
            assertEquals("Clean Code", response.getContent().get(0).getTitle());
        }

        @Test
        @DisplayName("Should filter by genre")
        void shouldFilterByGenre() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Book> bookPage = new PageImpl<>(List.of(testBook), pageable, 1);
            when(bookRepository.findByUserIdAndGenre(1L, Genre.TECHNOLOGY, pageable)).thenReturn(bookPage);

            // When
            PagedResponse<BookResponse> response = bookService.getUserBooks(1L, Genre.TECHNOLOGY, null, null, pageable);

            // Then
            assertNotNull(response);
            assertEquals(1, response.getContent().size());
            verify(bookRepository).findByUserIdAndGenre(1L, Genre.TECHNOLOGY, pageable);
        }

        @Test
        @DisplayName("Should filter by status")
        void shouldFilterByStatus() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Book> bookPage = new PageImpl<>(List.of(testBook), pageable, 1);
            when(bookRepository.findByUserIdAndStatus(1L, ReadingStatus.READING, pageable)).thenReturn(bookPage);

            // When
            PagedResponse<BookResponse> response = bookService.getUserBooks(1L, null, ReadingStatus.READING, null, pageable);

            // Then
            assertNotNull(response);
            verify(bookRepository).findByUserIdAndStatus(1L, ReadingStatus.READING, pageable);
        }

        @Test
        @DisplayName("Should filter by genre and status")
        void shouldFilterByGenreAndStatus() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Book> bookPage = new PageImpl<>(List.of(testBook), pageable, 1);
            when(bookRepository.findByUserIdAndGenreAndStatus(1L, Genre.TECHNOLOGY, ReadingStatus.READING, pageable))
                    .thenReturn(bookPage);

            // When
            PagedResponse<BookResponse> response = bookService.getUserBooks(1L, Genre.TECHNOLOGY, ReadingStatus.READING, null, pageable);

            // Then
            assertNotNull(response);
            verify(bookRepository).findByUserIdAndGenreAndStatus(1L, Genre.TECHNOLOGY, ReadingStatus.READING, pageable);
        }

        @Test
        @DisplayName("Should search by title")
        void shouldSearchByTitle() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Book> bookPage = new PageImpl<>(List.of(testBook), pageable, 1);
            when(bookRepository.findByUserIdAndTitleContainingIgnoreCase(1L, "Clean", pageable)).thenReturn(bookPage);

            // When
            PagedResponse<BookResponse> response = bookService.getUserBooks(1L, null, null, "Clean", pageable);

            // Then
            assertNotNull(response);
            verify(bookRepository).findByUserIdAndTitleContainingIgnoreCase(1L, "Clean", pageable);
        }
    }

    @Nested
    @DisplayName("updateBook tests")
    class UpdateBookTests {

        @Test
        @DisplayName("Should update book successfully")
        void shouldUpdateBookSuccessfully() {
            // Given
            BookRequest updateRequest = BookRequest.builder()
                    .title("Clean Code Updated")
                    .author("Robert C. Martin")
                    .genre(Genre.TECHNOLOGY)
                    .status(ReadingStatus.COMPLETED)
                    .build();

            when(bookRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testBook));
            when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            BookResponse response = bookService.updateBook(1L, updateRequest, 1L);

            // Then
            assertNotNull(response);
            assertEquals("Clean Code Updated", response.getTitle());
            assertEquals(ReadingStatus.COMPLETED, response.getStatus());
            verify(bookRepository).save(any(Book.class));
        }

        @Test
        @DisplayName("Should throw exception when book not found for update")
        void shouldThrowExceptionWhenBookNotFoundForUpdate() {
            // Given
            when(bookRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ResourceNotFoundException.class,
                    () -> bookService.updateBook(999L, bookRequest, 1L));
        }
    }

    @Nested
    @DisplayName("deleteBook tests")
    class DeleteBookTests {

        @Test
        @DisplayName("Should delete book successfully")
        void shouldDeleteBookSuccessfully() {
            // Given
            when(bookRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testBook));
            doNothing().when(bookRepository).delete(testBook);

            // When
            bookService.deleteBook(1L, 1L);

            // Then
            verify(bookRepository).delete(testBook);
        }

        @Test
        @DisplayName("Should throw exception when book not found for deletion")
        void shouldThrowExceptionWhenBookNotFoundForDeletion() {
            // Given
            when(bookRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ResourceNotFoundException.class,
                    () -> bookService.deleteBook(999L, 1L));
            verify(bookRepository, never()).delete(any(Book.class));
        }
    }

    @Nested
    @DisplayName("getBookCount tests")
    class GetBookCountTests {

        @Test
        @DisplayName("Should return book count for user")
        void shouldReturnBookCountForUser() {
            // Given
            when(bookRepository.countByUserId(1L)).thenReturn(5L);

            // When
            long count = bookService.getBookCount(1L);

            // Then
            assertEquals(5L, count);
        }

        @Test
        @DisplayName("Should return book count by status")
        void shouldReturnBookCountByStatus() {
            // Given
            when(bookRepository.countByUserIdAndStatus(1L, ReadingStatus.COMPLETED)).thenReturn(3L);

            // When
            long count = bookService.getBookCountByStatus(1L, ReadingStatus.COMPLETED);

            // Then
            assertEquals(3L, count);
        }
    }

    @Nested
    @DisplayName("quickSearch tests")
    class QuickSearchTests {

        @Test
        @DisplayName("Should search books by title or author")
        void shouldSearchBooksByTitleOrAuthor() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Book> bookPage = new PageImpl<>(List.of(testBook), pageable, 1);
            when(bookRepository.findByUserIdAndTitleContainingIgnoreCaseOrUserIdAndAuthorContainingIgnoreCase(
                    1L, "Martin", 1L, "Martin", pageable)).thenReturn(bookPage);

            // When
            PagedResponse<BookResponse> response = bookService.quickSearch("Martin", 1L, pageable);

            // Then
            assertNotNull(response);
            assertEquals(1, response.getContent().size());
        }
    }
}
