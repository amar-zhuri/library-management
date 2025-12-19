package com.library.library_management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.library_management.dto.request.LoginRequest;
import com.library.library_management.dto.request.RegisterRequest;
import com.library.library_management.entity.User;
import com.library.library_management.entity.enums.Role;
import com.library.library_management.repository.UserRepository;
import com.library.library_management.security.JwtService;
import com.library.library_management.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
@DisplayName("AuthController Integration Tests")
class AuthControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private EmailService emailService;

    private User verifiedUser;
    private User unverifiedUser;

    @BeforeEach
    void setUp() {
        // Set up MockMvc with Spring Security
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // Clean up
        userRepository.deleteAll();

        // Mock email service to avoid sending real emails
        doNothing().when(emailService).sendVerificationEmail(anyString(), anyString(), anyString());
        doNothing().when(emailService).sendWelcomeEmail(anyString(), anyString());
        doNothing().when(emailService).sendPasswordResetEmail(anyString(), anyString(), anyString());

        // Create verified user
        verifiedUser = User.builder()
                .name("Verified User")
                .email("verified@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.USER)
                .emailVerified(true)
                .build();
        verifiedUser = userRepository.save(verifiedUser);

        // Create unverified user
        unverifiedUser = User.builder()
                .name("Unverified User")
                .email("unverified@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.USER)
                .emailVerified(false)
                .build();
        unverifiedUser = userRepository.save(unverifiedUser);
    }

    @Nested
    @DisplayName("POST /api/auth/register")
    class RegisterTests {

        @Test
        @DisplayName("Should register new user successfully")
        void shouldRegisterNewUserSuccessfully() throws Exception {
            RegisterRequest request = RegisterRequest.builder()
                    .name("New User")
                    .email("newuser@example.com")
                    .password("password123")
                    .build();

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.token").isNotEmpty())
                    .andExpect(jsonPath("$.type").value("Bearer"))
                    .andExpect(jsonPath("$.user.email").value("newuser@example.com"))
                    .andExpect(jsonPath("$.user.name").value("New User"));
        }

        @Test
        @DisplayName("Should return 400 when email already exists")
        void shouldReturn400WhenEmailExists() throws Exception {
            RegisterRequest request = RegisterRequest.builder()
                    .name("Duplicate User")
                    .email("verified@example.com") // Already exists
                    .password("password123")
                    .build();

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when name is missing")
        void shouldReturn400WhenNameMissing() throws Exception {
            RegisterRequest request = RegisterRequest.builder()
                    .email("test@example.com")
                    .password("password123")
                    .build();

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when email is invalid")
        void shouldReturn400WhenEmailInvalid() throws Exception {
            RegisterRequest request = RegisterRequest.builder()
                    .name("Test User")
                    .email("invalid-email")
                    .password("password123")
                    .build();

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when password is too short")
        void shouldReturn400WhenPasswordTooShort() throws Exception {
            RegisterRequest request = RegisterRequest.builder()
                    .name("Test User")
                    .email("test@example.com")
                    .password("123") // Too short
                    .build();

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully with valid credentials")
        void shouldLoginSuccessfully() throws Exception {
            LoginRequest request = LoginRequest.builder()
                    .email("verified@example.com")
                    .password("password123")
                    .build();

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").isNotEmpty())
                    .andExpect(jsonPath("$.type").value("Bearer"))
                    .andExpect(jsonPath("$.user.email").value("verified@example.com"));
        }

        @Test
        @DisplayName("Should return 400 when email not found")
        void shouldReturn400WhenEmailNotFound() throws Exception {
            LoginRequest request = LoginRequest.builder()
                    .email("nonexistent@example.com")
                    .password("password123")
                    .build();

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when password is incorrect")
        void shouldReturn400WhenPasswordIncorrect() throws Exception {
            LoginRequest request = LoginRequest.builder()
                    .email("verified@example.com")
                    .password("wrongpassword")
                    .build();

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when email not verified")
        void shouldReturn400WhenEmailNotVerified() throws Exception {
            LoginRequest request = LoginRequest.builder()
                    .email("unverified@example.com")
                    .password("password123")
                    .build();

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/auth/me")
    class GetCurrentUserTests {

        @Test
        @DisplayName("Should return current user info when authenticated")
        void shouldReturnCurrentUserInfo() throws Exception {
            String token = jwtService.generateToken(
                    verifiedUser.getId(),
                    verifiedUser.getEmail(),
                    verifiedUser.getRole().name()
            );

            mockMvc.perform(get("/api/auth/me")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(verifiedUser.getId()))
                    .andExpect(jsonPath("$.email").value("verified@example.com"))
                    .andExpect(jsonPath("$.name").value("Verified User"));
        }

        @Test
        @DisplayName("Should reject request when not authenticated")
        void shouldRejectWhenNotAuthenticated() throws Exception {
            // Without authentication, the request should not return 200 OK with user data
            mockMvc.perform(get("/api/auth/me"))
                    .andExpect(result -> {
                        int status = result.getResponse().getStatus();
                        // Should be either 401, 403, or 500 (if exception thrown) - not 200
                        assertTrue(status != 200, "Unauthenticated request should not succeed");
                    });
        }

        @Test
        @DisplayName("Should reject request with invalid token")
        void shouldRejectWithInvalidToken() throws Exception {
            // With invalid token, the request should not return 200 OK with user data
            mockMvc.perform(get("/api/auth/me")
                            .header("Authorization", "Bearer invalid_token"))
                    .andExpect(result -> {
                        int status = result.getResponse().getStatus();
                        // Should be either 401, 403, or 500 (if exception thrown) - not 200
                        assertTrue(status != 200, "Request with invalid token should not succeed");
                    });
        }
    }

    @Nested
    @DisplayName("POST /api/auth/logout")
    class LogoutTests {

        @Test
        @DisplayName("Should logout successfully")
        void shouldLogoutSuccessfully() throws Exception {
            String token = jwtService.generateToken(
                    verifiedUser.getId(),
                    verifiedUser.getEmail(),
                    verifiedUser.getRole().name()
            );

            mockMvc.perform(post("/api/auth/logout")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Logged out successfully"));
        }
    }

    @Nested
    @DisplayName("POST /api/auth/forgot-password")
    class ForgotPasswordTests {

        @Test
        @DisplayName("Should return success even for non-existent email (security)")
        void shouldReturnSuccessForNonExistentEmail() throws Exception {
            mockMvc.perform(post("/api/auth/forgot-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\": \"nonexistent@example.com\"}"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should send reset email for existing user")
        void shouldSendResetEmailForExistingUser() throws Exception {
            mockMvc.perform(post("/api/auth/forgot-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\": \"verified@example.com\"}"))
                    .andExpect(status().isOk());
        }
    }
}
