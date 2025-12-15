package com.library.library_management.service;

import com.library.library_management.dto.response.RecommendationResponse;
import com.library.library_management.dto.response.RecommendationResponse.RecommendedBook;
import com.library.library_management.entity.Book;
import com.library.library_management.entity.enums.Genre;
import com.library.library_management.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RecommendationService {

    private final BookRepository bookRepository;

    private static final int DEFAULT_LIMIT = 5;

    /**
     * Get all recommendations for a user
     */
    public RecommendationResponse getRecommendations(Long userId) {
        log.info("Generating recommendations for user {}", userId);

        List<RecommendedBook> byGenre = getRecommendationsByGenre(userId, DEFAULT_LIMIT);
        List<RecommendedBook> byAuthor = getRecommendationsByAuthor(userId, DEFAULT_LIMIT);
        List<RecommendedBook> fromSimilarUsers = getRecommendationsFromSimilarUsers(userId, DEFAULT_LIMIT);

        String message = buildMessage(byGenre, byAuthor, fromSimilarUsers);

        return RecommendationResponse.builder()
                .byGenre(byGenre)
                .byAuthor(byAuthor)
                .fromSimilarUsers(fromSimilarUsers)
                .message(message)
                .build();
    }

    /**
     * Get recommendations based on user's favorite genre
     */
    public List<RecommendedBook> getRecommendationsByGenre(Long userId, int limit) {
        log.debug("Getting genre-based recommendations for user {}", userId);

        // Find user's favorite genre
        List<Object[]> genreStats = bookRepository.countBooksByGenreForUser(userId);

        if (genreStats.isEmpty()) {
            return List.of();
        }

        Genre favoriteGenre = (Genre) genreStats.get(0)[0];
        Long genreCount = (Long) genreStats.get(0)[1];

        // Find popular books in this genre that user doesn't own
        List<Book> recommendations = bookRepository.findPopularBooksInGenreNotOwnedByUser(
                userId, favoriteGenre, PageRequest.of(0, limit));

        String reason = String.format("Based on your favorite genre: %s (%d books)", 
                favoriteGenre, genreCount);

        return recommendations.stream()
                .map(book -> toRecommendedBook(book, reason))
                .collect(Collectors.toList());
    }

    /**
     * Get recommendations based on user's favorite authors
     */
    public List<RecommendedBook> getRecommendationsByAuthor(Long userId, int limit) {
        log.debug("Getting author-based recommendations for user {}", userId);

        // Find user's top authors
        List<Object[]> authorStats = bookRepository.findTopAuthorsForUser(userId, PageRequest.of(0, 3));

        if (authorStats.isEmpty()) {
            return List.of();
        }

        List<RecommendedBook> recommendations = new ArrayList<>();

        for (Object[] row : authorStats) {
            String author = (String) row[0];
            Long count = (Long) row[1];

            // Find books by this author that user doesn't own
            List<Book> authorBooks = findBooksByAuthorNotOwned(userId, author, 2);

            String reason = String.format("You have %d books by %s", count, author);

            for (Book book : authorBooks) {
                recommendations.add(toRecommendedBook(book, reason));

                if (recommendations.size() >= limit) {
                    return recommendations;
                }
            }
        }

        return recommendations;
    }

    /**
     * Get recommendations from users with similar taste
     */
    public List<RecommendedBook> getRecommendationsFromSimilarUsers(Long userId, int limit) {
        log.debug("Getting similar-user recommendations for user {}", userId);

        // Find users with similar books
        List<Long> similarUserIds = bookRepository.findUsersWithSimilarBooks(userId);

        if (similarUserIds.isEmpty()) {
            return List.of();
        }

        // Find books those users have that current user doesn't
        List<Book> recommendations = bookRepository.findBooksFromSimilarUsers(
                userId, similarUserIds, PageRequest.of(0, limit));

        String reason = "Users with similar taste enjoyed this";

        return recommendations.stream()
                .map(book -> toRecommendedBook(book, reason))
                .collect(Collectors.toList());
    }

    /**
     * Find books by author that user doesn't own
     */
    private List<Book> findBooksByAuthorNotOwned(Long userId, String author, int limit) {
        // Get all books by this author
        List<Book> allByAuthor = bookRepository.findAll().stream()
                .filter(b -> b.getAuthor().equalsIgnoreCase(author))
                .filter(b -> !b.getUser().getId().equals(userId))
                .limit(limit)
                .collect(Collectors.toList());

        return allByAuthor;
    }

    /**
     * Convert Book entity to RecommendedBook DTO
     */
    private RecommendedBook toRecommendedBook(Book book, String reason) {
        return RecommendedBook.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .genre(book.getGenre())
                .pageCount(book.getPageCount())
                .publicationYear(book.getPublicationYear())
                .reason(reason)
                .build();
    }

    /**
     * Build summary message
     */
    private String buildMessage(List<RecommendedBook> byGenre,
                                 List<RecommendedBook> byAuthor,
                                 List<RecommendedBook> fromSimilarUsers) {

        int total = byGenre.size() + byAuthor.size() + fromSimilarUsers.size();

        if (total == 0) {
            return "Add more books to get personalized recommendations!";
        }

        return String.format("We found %d recommendations for you!", total);
    }
}