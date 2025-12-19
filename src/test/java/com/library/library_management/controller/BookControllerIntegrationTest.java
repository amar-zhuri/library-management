package com.library.library_management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.library_management.dto.request.BookRequest;
import com.library.library_management.entity.Book;
import com.library.library_management.entity.User;
import com.library.library_management.entity.enums.Genre;
import com.library.library_management.entity.enums.ReadingStatus;
import com.library.library_management.entity.enums.Role;
import com.library.library_management.repository.BookRepository;
import com.library.library_management.repository.UserRepository;
import com.library.library_management.security.CustomUserDetails;
import com.library.library_management.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
@DisplayName("BookController Integration Tests")
class BookControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private JwtService jwtService;

    private User testUser;
    private String authToken;
    private Book testBook;

    @BeforeEach
    void setUp() {
        // Set up MockMvc with Spring Security
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // Clean up
        bookRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user
        testUser = User.builder()
                .name("Test User")
                .email("testuser@example.com")
                .password("$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG") // "password"
                .role(Role.USER)
                .emailVerified(true)
                .build();
        testUser = userRepository.save(testUser);

        // Generate JWT token
        authToken = jwtService.generateToken(testUser.getId(), testUser.getEmail(), testUser.getRole().name());

        // Create test book
        testBook = Book.builder()
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
                .build();
        testBook = bookRepository.save(testBook);

        // Set up security context
        CustomUserDetails userDetails = new CustomUserDetails(testUser);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );
    }

    @Nested
    @DisplayName("POST /api/books")
    class CreateBookTests {

        @Test
        @DisplayName("Should create book successfully")
        void shouldCreateBookSuccessfully() throws Exception {
            BookRequest request = BookRequest.builder()
                    .title("The Pragmatic Programmer")
                    .author("David Thomas")
                    .genre(Genre.TECHNOLOGY)
                    .status(ReadingStatus.TO_READ)
                    .price(new BigDecimal("49.99"))
                    .pageCount(352)
                    .publicationYear(2019)
                    .build();

            mockMvc.perform(post("/api/books")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.title").value("The Pragmatic Programmer"))
                    .andExpect(jsonPath("$.author").value("David Thomas"))
                    .andExpect(jsonPath("$.genre").value("TECHNOLOGY"))
                    .andExpect(jsonPath("$.id").isNotEmpty());
        }

        @Test
        @DisplayName("Should return 400 when title is missing")
        void shouldReturn400WhenTitleMissing() throws Exception {
            BookRequest request = BookRequest.builder()
                    .author("Test Author")
                    .genre(Genre.FICTION)
                    .build();

            mockMvc.perform(post("/api/books")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 401/403 when not authenticated")
        void shouldRejectWhenNotAuthenticated() throws Exception {
            // Clear security context for this test
            SecurityContextHolder.clearContext();

            BookRequest request = BookRequest.builder()
                    .title("Test Book")
                    .author("Test Author")
                    .genre(Genre.FICTION)
                    .build();

            mockMvc.perform(post("/api/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden()); // 403 when no authentication present
        }
    }

    @Nested
    @DisplayName("GET /api/books")
    class GetBooksTests {

        @Test
        @DisplayName("Should return paginated books for user")
        void shouldReturnPaginatedBooksForUser() throws Exception {
            mockMvc.perform(get("/api/books")
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                    .andExpect(jsonPath("$.content[0].title").value("Clean Code"));
        }

        @Test
        @DisplayName("Should filter books by genre")
        void shouldFilterBooksByGenre() throws Exception {
            mockMvc.perform(get("/api/books")
                            .header("Authorization", "Bearer " + authToken)
                            .param("genre", "TECHNOLOGY"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].genre").value("TECHNOLOGY"));
        }

        @Test
        @DisplayName("Should filter books by status")
        void shouldFilterBooksByStatus() throws Exception {
            mockMvc.perform(get("/api/books")
                            .header("Authorization", "Bearer " + authToken)
                            .param("status", "READING"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].status").value("READING"));
        }

        @Test
        @DisplayName("Should search books by title")
        void shouldSearchBooksByTitle() throws Exception {
            mockMvc.perform(get("/api/books")
                            .header("Authorization", "Bearer " + authToken)
                            .param("search", "Clean"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].title", containsString("Clean")));
        }
    }

    @Nested
    @DisplayName("GET /api/books/{id}")
    class GetBookByIdTests {

        @Test
        @DisplayName("Should return book by id")
        void shouldReturnBookById() throws Exception {
            mockMvc.perform(get("/api/books/" + testBook.getId())
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(testBook.getId()))
                    .andExpect(jsonPath("$.title").value("Clean Code"));
        }

        @Test
        @DisplayName("Should return 404 when book not found")
        void shouldReturn404WhenBookNotFound() throws Exception {
            mockMvc.perform(get("/api/books/99999")
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/books/{id}")
    class UpdateBookTests {

        @Test
        @DisplayName("Should update book successfully")
        void shouldUpdateBookSuccessfully() throws Exception {
            BookRequest updateRequest = BookRequest.builder()
                    .title("Clean Code - Updated")
                    .author("Robert C. Martin")
                    .genre(Genre.TECHNOLOGY)
                    .status(ReadingStatus.COMPLETED)
                    .price(new BigDecimal("34.99"))
                    .pageCount(464)
                    .publicationYear(2008)
                    .build();

            mockMvc.perform(put("/api/books/" + testBook.getId())
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Clean Code - Updated"))
                    .andExpect(jsonPath("$.status").value("COMPLETED"));
        }

        @Test
        @DisplayName("Should return 404 when updating non-existent book")
        void shouldReturn404WhenUpdatingNonExistentBook() throws Exception {
            BookRequest updateRequest = BookRequest.builder()
                    .title("Test")
                    .author("Test")
                    .genre(Genre.FICTION)
                    .build();

            mockMvc.perform(put("/api/books/99999")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/books/{id}")
    class DeleteBookTests {

        @Test
        @DisplayName("Should delete book successfully")
        void shouldDeleteBookSuccessfully() throws Exception {
            mockMvc.perform(delete("/api/books/" + testBook.getId())
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isNoContent());

            // Verify book is deleted
            mockMvc.perform(get("/api/books/" + testBook.getId())
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent book")
        void shouldReturn404WhenDeletingNonExistentBook() throws Exception {
            mockMvc.perform(delete("/api/books/99999")
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/books/stats")
    class GetBookStatsTests {

        @Test
        @DisplayName("Should return book statistics")
        void shouldReturnBookStatistics() throws Exception {
            mockMvc.perform(get("/api/books/stats")
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total").isNumber())
                    .andExpect(jsonPath("$.toRead").isNumber())
                    .andExpect(jsonPath("$.reading").isNumber())
                    .andExpect(jsonPath("$.completed").isNumber());
        }
    }

    @Nested
    @DisplayName("GET /api/books/quick-search")
    class QuickSearchTests {

        @Test
        @DisplayName("Should search books by query")
        void shouldSearchBooksByQuery() throws Exception {
            mockMvc.perform(get("/api/books/quick-search")
                            .header("Authorization", "Bearer " + authToken)
                            .param("q", "Martin"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }
    }
}
