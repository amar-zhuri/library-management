package com.library.library_management.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtService Unit Tests")
class JwtServiceTest {

    private JwtService jwtService;
    private static final String TEST_SECRET = "testSecretKeyThatIsAtLeast256BitsLongForHS256AlgorithmTestingPurposes2024!";
    private static final long TEST_EXPIRATION = 86400000L; // 24 hours

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretString", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", TEST_EXPIRATION);
        jwtService.init();
    }

    @Test
    @DisplayName("Should generate a valid JWT token")
    void shouldGenerateValidToken() {
        // Given
        Long userId = 1L;
        String email = "test@example.com";
        String role = "USER";

        // When
        String token = jwtService.generateToken(userId, email, role);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
    }

    @Test
    @DisplayName("Should extract email from token")
    void shouldExtractEmailFromToken() {
        // Given
        Long userId = 1L;
        String email = "test@example.com";
        String role = "USER";
        String token = jwtService.generateToken(userId, email, role);

        // When
        String extractedEmail = jwtService.extractEmail(token);

        // Then
        assertEquals(email, extractedEmail);
    }

    @Test
    @DisplayName("Should extract userId from token")
    void shouldExtractUserIdFromToken() {
        // Given
        Long userId = 123L;
        String email = "test@example.com";
        String role = "USER";
        String token = jwtService.generateToken(userId, email, role);

        // When
        Long extractedUserId = jwtService.extractUserId(token);

        // Then
        assertEquals(userId, extractedUserId);
    }

    @Test
    @DisplayName("Should extract role from token")
    void shouldExtractRoleFromToken() {
        // Given
        Long userId = 1L;
        String email = "test@example.com";
        String role = "ADMIN";
        String token = jwtService.generateToken(userId, email, role);

        // When
        String extractedRole = jwtService.extractRole(token);

        // Then
        assertEquals(role, extractedRole);
    }

    @Test
    @DisplayName("Should validate a valid token")
    void shouldValidateValidToken() {
        // Given
        String token = jwtService.generateToken(1L, "test@example.com", "USER");

        // When
        boolean isValid = jwtService.validateToken(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should reject an invalid token")
    void shouldRejectInvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        boolean isValid = jwtService.validateToken(invalidToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should reject a tampered token")
    void shouldRejectTamperedToken() {
        // Given
        String token = jwtService.generateToken(1L, "test@example.com", "USER");
        String tamperedToken = token.substring(0, token.length() - 5) + "xxxxx";

        // When
        boolean isValid = jwtService.validateToken(tamperedToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should extract expiration date from token")
    void shouldExtractExpirationFromToken() {
        // Given
        String token = jwtService.generateToken(1L, "test@example.com", "USER");

        // When
        var expiration = jwtService.extractExpiration(token);

        // Then
        assertNotNull(expiration);
        assertTrue(expiration.getTime() > System.currentTimeMillis());
    }

    @Test
    @DisplayName("Should return correct expiration time")
    void shouldReturnCorrectExpirationTime() {
        // When
        long expirationTime = jwtService.getExpirationTime();

        // Then
        assertEquals(TEST_EXPIRATION, expirationTime);
    }

    @Test
    @DisplayName("Should get expiry time as Instant")
    void shouldGetExpiryTimeAsInstant() {
        // Given
        String token = jwtService.generateToken(1L, "test@example.com", "USER");

        // When
        var expiryInstant = jwtService.getExpiryTime(token);

        // Then
        assertNotNull(expiryInstant);
        assertTrue(expiryInstant.isAfter(java.time.Instant.now()));
    }
}
