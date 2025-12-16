package com.library.library_management.service;

import com.library.library_management.dto.response.UserInsightsResponse;
import com.library.library_management.entity.enums.Genre;
import com.library.library_management.entity.enums.ReadingStatus;
import com.library.library_management.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserInsightsService {

    private final BookRepository bookRepository;

    /**
     * Generate rule-based insights for a user
     */
    public UserInsightsResponse getInsights(Long userId) {
        long startTime = System.currentTimeMillis();
        log.info("Generating rule-based insights for user {}", userId);

        List<String> insights = new ArrayList<>();

        // Total books
        long totalBooks = bookRepository.countByUserId(userId);

        if (totalBooks == 0) {
            return UserInsightsResponse.builder()
                    .insights(List.of("Start adding books to see your reading insights!"))
                    .summary("Your library is empty. Add some books to get started!")
                    .generatedBy("RULE_BASED")
                    .generationTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }

        insights.add(String.format("You have %d books in your library", totalBooks));

        // Favorite genre
        List<Object[]> genreStats = bookRepository.countBooksByGenreForUser(userId);
        if (!genreStats.isEmpty()) {
            Genre topGenre = (Genre) genreStats.get(0)[0];
            Long topGenreCount = (Long) genreStats.get(0)[1];
            int percentage = (int) ((topGenreCount * 100) / totalBooks);
            insights.add(String.format("%s is your most-read genre (%d%% of your library)", 
                    formatGenre(topGenre), percentage));
        }

        // Favorite author
        List<Object[]> authorStats = bookRepository.findTopAuthorsForUser(userId, PageRequest.of(0, 1));
        if (!authorStats.isEmpty()) {
            String topAuthor = (String) authorStats.get(0)[0];
            Long authorCount = (Long) authorStats.get(0)[1];
            insights.add(String.format("%s is your favorite author with %d books", 
                    topAuthor, authorCount));
        }

        // Completion rate
        List<Object[]> statusStats = bookRepository.countBooksByStatusForUser(userId);
        long completed = 0;
        long reading = 0;
        long toRead = 0;

        for (Object[] row : statusStats) {
            ReadingStatus status = (ReadingStatus) row[0];
            Long count = (Long) row[1];

            switch (status) {
                case COMPLETED -> completed = count;
                case READING -> reading = count;
                case TO_READ -> toRead = count;
            }
        }

        if (completed > 0) {
            int completionRate = (int) ((completed * 100) / totalBooks);
            insights.add(String.format("You've completed %d out of %d books (%d%%)", 
                    completed, totalBooks, completionRate));
        }

        if (reading > 0) {
            insights.add(String.format("You're currently reading %d books", reading));
        }

        if (toRead > 0) {
            insights.add(String.format("You have %d books in your to-read list", toRead));
        }

        // Library value
        BigDecimal totalValue = bookRepository.calculateLibraryValueForUser(userId);
        if (totalValue != null && totalValue.compareTo(BigDecimal.ZERO) > 0) {
            insights.add(String.format("Your library is worth $%.2f total", totalValue));
        }

        // Genre diversity
        int genreCount = genreStats.size();
        if (genreCount >= 4) {
            insights.add(String.format("You read across %d different genres - diverse taste!", genreCount));
        } else if (genreCount == 1) {
            insights.add("You're focused on one genre - a true specialist!");
        }

        // Build summary
        String summary = buildSummary(totalBooks, genreStats, completed, totalBooks);

        long generationTime = System.currentTimeMillis() - startTime;
        log.info("Generated {} insights in {}ms", insights.size(), generationTime);

        return UserInsightsResponse.builder()
                .insights(insights)
                .summary(summary)
                .generatedBy("RULE_BASED")
                .generationTimeMs(generationTime)
                .build();
    }

    /**
     * Format genre name nicely
     */
    private String formatGenre(Genre genre) {
        String name = genre.name().replace("_", " ");
        return name.charAt(0) + name.substring(1).toLowerCase();
    }

    /**
     * Build summary sentence
     */
    private String buildSummary(long totalBooks, List<Object[]> genreStats, 
                                 long completed, long total) {
        StringBuilder summary = new StringBuilder();

        if (totalBooks <= 5) {
            summary.append("You're just getting started with your library. ");
        } else if (totalBooks <= 20) {
            summary.append("You have a growing book collection. ");
        } else {
            summary.append("You're a serious book collector! ");
        }

        if (!genreStats.isEmpty()) {
            Genre topGenre = (Genre) genreStats.get(0)[0];
            summary.append(formatGenre(topGenre)).append(" is clearly your favorite. ");
        }

        if (total > 0) {
            int completionRate = (int) ((completed * 100) / total);
            if (completionRate >= 80) {
                summary.append("Great job finishing what you start!");
            } else if (completionRate >= 50) {
                summary.append("Keep up the reading momentum!");
            } else {
                summary.append("Lots of adventures still waiting for you!");
            }
        }

        return summary.toString().trim();
    }
}