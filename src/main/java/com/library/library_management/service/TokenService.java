package com.library.library_management.service;

import com.library.library_management.entity.User;
import com.library.library_management.entity.VerificationToken;
import com.library.library_management.entity.enums.TokenType;
import com.library.library_management.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

    private final VerificationTokenRepository tokenRepository;

    @Value("${app.email.verification-token-expiry}")
    private long verificationTokenExpiry;

    @Value("${app.email.password-reset-token-expiry}")
    private long passwordResetTokenExpiry;

    /**
     * Create a new verification token for email verification
     */
    @Transactional
    public String createEmailVerificationToken(User user) {
        // Invalidate any existing tokens
        tokenRepository.invalidateUserTokens(user.getId(), TokenType.EMAIL_VERIFICATION, LocalDateTime.now());

        // Create new token
        String token = generateToken();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .tokenType(TokenType.EMAIL_VERIFICATION)
                .user(user)
                .expiresAt(LocalDateTime.now().plusSeconds(verificationTokenExpiry / 1000))
                .build();

        tokenRepository.save(verificationToken);
        log.info("Created email verification token for user: {}", user.getEmail());

        return token;
    }

    /**
     * Create a new token for password reset
     */
    @Transactional
    public String createPasswordResetToken(User user) {
        // Invalidate any existing tokens
        tokenRepository.invalidateUserTokens(user.getId(), TokenType.PASSWORD_RESET, LocalDateTime.now());

        // Create new token
        String token = generateToken();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .tokenType(TokenType.PASSWORD_RESET)
                .user(user)
                .expiresAt(LocalDateTime.now().plusSeconds(passwordResetTokenExpiry / 1000))
                .build();

        tokenRepository.save(verificationToken);
        log.info("Created password reset token for user: {}", user.getEmail());

        return token;
    }

    /**
     * Validate and get token
     */
    public Optional<VerificationToken> validateToken(String token, TokenType tokenType) {
        Optional<VerificationToken> verificationToken = tokenRepository.findByTokenAndTokenType(token, tokenType);

        if (verificationToken.isEmpty()) {
            log.warn("Token not found: {}", token);
            return Optional.empty();
        }

        VerificationToken vt = verificationToken.get();

        if (!vt.isValid()) {
            log.warn("Token is invalid (expired or used): {}", token);
            return Optional.empty();
        }

        return verificationToken;
    }

    /**
     * Mark token as used
     */
    @Transactional
    public void markTokenAsUsed(VerificationToken token) {
        token.setUsedAt(LocalDateTime.now());
        tokenRepository.save(token);
        log.info("Token marked as used: {}", token.getToken());
    }

    /**
     * Generate a secure random token
     */
    private String generateToken() {
        return UUID.randomUUID().toString() + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Cleanup expired tokens (runs every hour)
     */
    @Scheduled(fixedRate = 3600000) // Every hour
    @Transactional
    public void cleanupExpiredTokens() {
        int deleted = tokenRepository.deleteExpiredTokens(LocalDateTime.now());
        if (deleted > 0) {
            log.info("Cleaned up {} expired tokens", deleted);
        }
    }
}