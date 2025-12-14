package com.library.library_management.repository;

import com.library.library_management.entity.VerificationToken;
import com.library.library_management.entity.enums.TokenType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken> findByTokenAndTokenType(String token, TokenType tokenType);

    List<VerificationToken> findByUserIdAndTokenType(Long userId, TokenType tokenType);

    // Find valid (unused, not expired) token for a user
    @Query("SELECT t FROM VerificationToken t WHERE t.user.id = :userId " +
           "AND t.tokenType = :tokenType AND t.usedAt IS NULL " +
           "AND t.expiresAt > :now ORDER BY t.createdAt DESC")
    List<VerificationToken> findValidTokensByUserAndType(
            @Param("userId") Long userId,
            @Param("tokenType") TokenType tokenType,
            @Param("now") LocalDateTime now);

    // Delete expired tokens (cleanup job)
    @Modifying
    @Query("DELETE FROM VerificationToken t WHERE t.expiresAt < :now")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);

    // Invalidate all tokens of a type for a user (when new one is created)
    @Modifying
    @Query("UPDATE VerificationToken t SET t.usedAt = :now " +
           "WHERE t.user.id = :userId AND t.tokenType = :tokenType AND t.usedAt IS NULL")
    int invalidateUserTokens(
            @Param("userId") Long userId,
            @Param("tokenType") TokenType tokenType,
            @Param("now") LocalDateTime now);
}