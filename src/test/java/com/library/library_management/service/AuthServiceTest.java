package com.library.library_management.service;

import com.library.library_management.dto.request.LoginRequest;
import com.library.library_management.dto.request.RegisterRequest;
import com.library.library_management.dto.response.AuthResponse;
import com.library.library_management.entity.User;
import com.library.library_management.entity.VerificationToken;
import com.library.library_management.entity.enums.Role;
import com.library.library_management.entity.enums.TokenType;
import com.library.library_management.exception.ResourceNotFoundException;
import com.library.library_management.repository.UserRepository;
import com.library.library_management.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private TokenService tokenService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

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

        registerRequest = RegisterRequest.builder()
                .name("New User")
                .email("newuser@example.com")
                .password("password123")
                .build();

        loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();
    }

    @Nested
    @DisplayName("register tests")
    class RegisterTests {

        @Test
        @DisplayName("Should register new user successfully")
        void shouldRegisterNewUserSuccessfully() {
            // Given
            when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encoded_password");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(1L);
                return user;
            });
            when(tokenService.createEmailVerificationToken(any(User.class))).thenReturn("verification_token");
            doNothing().when(emailService).sendVerificationEmail(anyString(), anyString(), anyString());
            when(jwtService.generateToken(any(), anyString(), anyString())).thenReturn("jwt_token");
            when(jwtService.getExpirationTime()).thenReturn(86400000L);

            // When
            AuthResponse response = authService.register(registerRequest);

            // Then
            assertNotNull(response);
            assertEquals("jwt_token", response.getToken());
            assertEquals("Bearer", response.getType());
            assertNotNull(response.getUser());
            assertEquals("newuser@example.com", response.getUser().getEmail());
            verify(userRepository).save(any(User.class));
            verify(emailService).sendVerificationEmail(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void shouldThrowExceptionWhenEmailExists() {
            // Given
            when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> authService.register(registerRequest));
            assertEquals("Email already registered", exception.getMessage());
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("login tests")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully with valid credentials")
        void shouldLoginSuccessfullyWithValidCredentials() {
            // Given
            when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(true);
            when(jwtService.generateToken(testUser.getId(), testUser.getEmail(), testUser.getRole().name()))
                    .thenReturn("jwt_token");
            when(jwtService.getExpirationTime()).thenReturn(86400000L);

            // When
            AuthResponse response = authService.login(loginRequest);

            // Then
            assertNotNull(response);
            assertEquals("jwt_token", response.getToken());
            assertEquals("Bearer", response.getType());
            assertEquals(testUser.getEmail(), response.getUser().getEmail());
        }

        @Test
        @DisplayName("Should throw exception when email not found")
        void shouldThrowExceptionWhenEmailNotFound() {
            // Given
            when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
            LoginRequest request = LoginRequest.builder()
                    .email("nonexistent@example.com")
                    .password("password")
                    .build();

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> authService.login(request));
            assertEquals("Invalid email or password", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when password is incorrect")
        void shouldThrowExceptionWhenPasswordIncorrect() {
            // Given
            when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(false);

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> authService.login(loginRequest));
            assertEquals("Invalid email or password", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when email not verified")
        void shouldThrowExceptionWhenEmailNotVerified() {
            // Given
            testUser.setEmailVerified(false);
            when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(true);

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> authService.login(loginRequest));
            assertTrue(exception.getMessage().contains("verify your email"));
        }
    }

    @Nested
    @DisplayName("verifyEmail tests")
    class VerifyEmailTests {

        @Test
        @DisplayName("Should verify email successfully")
        void shouldVerifyEmailSuccessfully() {
            // Given
            testUser.setEmailVerified(false);
            VerificationToken vt = VerificationToken.builder()
                    .token("valid_token")
                    .user(testUser)
                    .build();

            when(tokenService.validateToken("valid_token", TokenType.EMAIL_VERIFICATION))
                    .thenReturn(Optional.of(vt));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            doNothing().when(tokenService).markTokenAsUsed(any(VerificationToken.class));
            doNothing().when(emailService).sendWelcomeEmail(anyString(), anyString());

            // When
            authService.verifyEmail("valid_token");

            // Then
            assertTrue(testUser.getEmailVerified());
            verify(userRepository).save(testUser);
            verify(tokenService).markTokenAsUsed(vt);
            verify(emailService).sendWelcomeEmail(testUser.getEmail(), testUser.getName());
        }

        @Test
        @DisplayName("Should throw exception for invalid token")
        void shouldThrowExceptionForInvalidToken() {
            // Given
            when(tokenService.validateToken("invalid_token", TokenType.EMAIL_VERIFICATION))
                    .thenReturn(Optional.empty());

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> authService.verifyEmail("invalid_token"));
            assertEquals("Invalid or expired verification token", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when email already verified")
        void shouldThrowExceptionWhenEmailAlreadyVerified() {
            // Given
            testUser.setEmailVerified(true);
            VerificationToken vt = VerificationToken.builder()
                    .token("valid_token")
                    .user(testUser)
                    .build();

            when(tokenService.validateToken("valid_token", TokenType.EMAIL_VERIFICATION))
                    .thenReturn(Optional.of(vt));

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> authService.verifyEmail("valid_token"));
            assertEquals("Email is already verified", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("forgotPassword tests")
    class ForgotPasswordTests {

        @Test
        @DisplayName("Should send reset email when user exists")
        void shouldSendResetEmailWhenUserExists() {
            // Given
            when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
            when(tokenService.createPasswordResetToken(testUser)).thenReturn("reset_token");
            doNothing().when(emailService).sendPasswordResetEmail(anyString(), anyString(), anyString());

            // When
            authService.forgotPassword(testUser.getEmail());

            // Then
            verify(tokenService).createPasswordResetToken(testUser);
            verify(emailService).sendPasswordResetEmail(testUser.getEmail(), testUser.getName(), "reset_token");
        }

        @Test
        @DisplayName("Should not throw exception when email not found (security)")
        void shouldNotThrowExceptionWhenEmailNotFound() {
            // Given
            when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

            // When & Then - should not throw (to prevent email enumeration)
            assertDoesNotThrow(() -> authService.forgotPassword("nonexistent@example.com"));
            verify(tokenService, never()).createPasswordResetToken(any());
            verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("getCurrentUser tests")
    class GetCurrentUserTests {

        @Test
        @DisplayName("Should return current user info")
        void shouldReturnCurrentUserInfo() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            // When
            AuthResponse.UserInfo userInfo = authService.getCurrentUser(1L);

            // Then
            assertNotNull(userInfo);
            assertEquals(testUser.getId(), userInfo.getId());
            assertEquals(testUser.getEmail(), userInfo.getEmail());
            assertEquals(testUser.getName(), userInfo.getName());
            assertEquals(testUser.getRole(), userInfo.getRole());
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ResourceNotFoundException.class,
                    () -> authService.getCurrentUser(999L));
        }
    }
}
