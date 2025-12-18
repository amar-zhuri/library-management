package com.library.library_management.service.ai;

import com.library.library_management.dto.response.AIQueryResponse;
import com.library.library_management.dto.response.BookResponse;
import com.library.library_management.dto.response.InsightResponse;
import com.library.library_management.entity.Book;
import com.library.library_management.entity.enums.Genre;
import com.library.library_management.entity.enums.QueryType;
import com.library.library_management.entity.enums.ReadingStatus;
import com.library.library_management.repository.BookRepository;
import com.library.library_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIQueryService {

    private final RuleBasedQueryParser ruleBasedParser;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final LLMService llmService;

    // Setter for optional LLM service injection
    // public void setLlmService(LLMService llmService) {
    //     this.llmService = llmService;
    // }


    /**
     * Process a natural language query
     */
    public AIQueryResponse processQuery(String question, Long userId, boolean forceLLM) {
        long startTime = System.currentTimeMillis();
        log.info("Processing AI query: '{}' for user {}", question, userId);

        // First, try rule-based parsing
        QueryIntent intent = ruleBasedParser.parse(question);

        AIQueryResponse response;

        if (intent.isRecognized() && !forceLLM) {
            // Rule-based handling
            response = executeRuleBasedQuery(intent, userId);
            response.setProcessingMethod("RULE_BASED");
        } else if (llmService != null) {
            // Try LLM fallback
            response = executeLLMQuery(question, userId);
            response.setProcessingMethod("LLM");
        } else {
            // No LLM available, return suggestions
            response = buildUnrecognizedResponse(question);
        }

        response.setQuestion(question);
        response.setExecutionTimeMs(System.currentTimeMillis() - startTime);

        log.info("Query processed in {}ms using {}", response.getExecutionTimeMs(), response.getProcessingMethod());
        return response;
    }

    /**
     * Execute a rule-based query
     */
    private AIQueryResponse executeRuleBasedQuery(QueryIntent intent, Long userId) {
        QueryType type = intent.getQueryType();
        int limit = intent.getLimit() != null ? intent.getLimit() : 5;

        Object data;
        String answer;

        switch (type) {
            case TOP_READERS -> {
                data = getTopReaders(limit);
                answer = formatTopReadersAnswer((List<Map<String, Object>>) data);
            }
            case POPULAR_BOOKS -> {
                data = getPopularBooks(limit);
                answer = formatPopularBooksAnswer((List<Map<String, Object>>) data);
            }
            case EXPENSIVE_BOOKS -> {
                data = getExpensiveBooks(limit);
                answer = formatExpensiveBooksAnswer((List<BookResponse>) data);
            }
            case TOP_AUTHORS -> {
                boolean personal = intent.getOriginalQuestion() != null && intent.getOriginalQuestion().toLowerCase().contains("my");
                data = personal ? getTopAuthorsForUser(userId, limit) : getTopAuthors(limit);
                answer = formatTopAuthorsAnswer((List<Map<String, Object>>) data);
            }
            case GENRE_DISTRIBUTION -> {
                data = getGenreDistribution();
                answer = formatGenreDistributionAnswer((Map<String, Long>) data);
            }
            case STATUS_DISTRIBUTION -> {
                data = getStatusDistribution();
                answer = formatStatusDistributionAnswer((Map<String, Long>) data);
            }
            case TOTAL_BOOKS -> {
                long count = bookRepository.count();
                data = count;
                answer = String.format("There are %d books in the library system.", count);
            }
            case TOTAL_USERS -> {
                long count = userRepository.count();
                data = count;
                answer = String.format("There are %d registered users in the system.", count);
            }
            case USER_BOOK_COUNT -> {
                long count = bookRepository.countByUserId(userId);
                data = count;
                answer = String.format("You have %d books in your library.", count);
            }
            case USER_GENRE_DISTRIBUTION -> {
                data = getUserGenreDistribution(userId);
                answer = formatUserGenreDistributionAnswer((Map<String, Long>) data);
            }
            case USER_READING_STATS -> {
                data = getUserReadingStats(userId);
                answer = formatUserReadingStatsAnswer((Map<String, Object>) data);
            }
            case USER_LIBRARY_VALUE -> {
                BigDecimal value = bookRepository.calculateLibraryValueForUser(userId);
                data = value;
                answer = String.format("Your library is worth $%.2f in total.", value);
            }
            case USER_BOOKS_BY_STATUS -> {
                ReadingStatus status = intent.getStatus() != null ? intent.getStatus() : ReadingStatus.COMPLETED;
                List<Book> books = bookRepository.findByUserIdAndStatus(userId, status);
                data = books.stream().map(BookResponse::fromEntity).collect(Collectors.toList());
                answer = String.format("You have %d books with status '%s'.", books.size(), status);
            }
            case USER_RECENT_BOOKS -> {
                data = getRecentBooks(userId, limit);
                answer = String.format("Here are your %d most recently added books.", Math.min(limit, ((List<?>) data).size()));
            }
            case RECOMMENDATIONS_BY_GENRE -> {
                Genre genre = intent.getGenre();
                data = getRecommendationsByGenre(userId, genre, limit);
                answer = formatRecommendationsAnswer((List<BookResponse>) data, genre);
            }
            default -> {
                data = null;
                answer = "I couldn't understand that question. Please try rephrasing.";
            }
        }

        return AIQueryResponse.builder()
                .queryType(type)
                .answer(answer)
                .data(data)
                .recognizedQuery(true)
                .confidence(intent.getConfidence())
                .build();
    }

    /**
     * Execute an LLM-based query (when rule-based fails)
     */
    private AIQueryResponse executeLLMQuery(String question, Long userId) {
        if (!llmService.isAvailable()) {
            return buildUnrecognizedResponse(question);
        }

        try {
            return llmService.processQuery(question, userId);
        } catch (Exception e) {
            log.error("LLM query failed: {}", e.getMessage());
            return buildUnrecognizedResponse(question);
        }
    }

    /**
     * Build response for unrecognized queries
     */
    private AIQueryResponse buildUnrecognizedResponse(String question) {
        return AIQueryResponse.builder()
                .queryType(QueryType.UNKNOWN)
                .answer("I'm not sure how to answer that question. Here are some things you can ask me:")
                .recognizedQuery(false)
                .confidence(0.0)
                .suggestions(ruleBasedParser.getSuggestions())
                .build();
    }

    // ========== Data Retrieval Methods ==========

    private List<Map<String, Object>> getTopReaders(int limit) {
        return bookRepository.countBooksPerUser().stream()
                .limit(limit)
                .map(row -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("userId", row[0]);
                    map.put("userName", row[1]);
                    map.put("bookCount", row[2]);
                    return map;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> getPopularBooks(int limit) {
        return bookRepository.findMostPopularBooks(PageRequest.of(0, limit)).stream()
                .map(row -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("title", row[0]);
                    map.put("author", row[1]);
                    map.put("ownerCount", row[2]);
                    return map;
                })
                .collect(Collectors.toList());
    }

    private List<BookResponse> getExpensiveBooks(int limit) {
        return bookRepository.findMostExpensiveBooks(PageRequest.of(0, limit)).stream()
                .map(BookResponse::fromEntityWithOwner)
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> getTopAuthors(int limit) {
        return bookRepository.findTopAuthors(PageRequest.of(0, limit)).stream()
                .map(row -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("author", row[0]);
                    map.put("bookCount", row[1]);
                    return map;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> getTopAuthorsForUser(Long userId, int limit) {
        return bookRepository.findTopAuthorsForUser(userId, PageRequest.of(0, limit)).stream()
                .map(row -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("author", row[0]);
                    map.put("bookCount", row[1]);
                    return map;
                })
                .collect(Collectors.toList());
    }

    private Map<String, Long> getGenreDistribution() {
        Map<String, Long> distribution = new LinkedHashMap<>();
        bookRepository.countBooksByGenre().forEach(row ->
                distribution.put(row[0].toString(), (Long) row[1]));
        return distribution;
    }

    private Map<String, Long> getStatusDistribution() {
        Map<String, Long> distribution = new LinkedHashMap<>();
        bookRepository.countBooksByStatus().forEach(row ->
                distribution.put(row[0].toString(), (Long) row[1]));
        return distribution;
    }

    private Map<String, Long> getUserGenreDistribution(Long userId) {
        Map<String, Long> distribution = new LinkedHashMap<>();
        bookRepository.countBooksByGenreForUser(userId).forEach(row ->
                distribution.put(row[0].toString(), (Long) row[1]));
        return distribution;
    }

    private Map<String, Object> getUserReadingStats(Long userId) {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalBooks", bookRepository.countByUserId(userId));
        stats.put("toRead", bookRepository.countByUserIdAndStatus(userId, ReadingStatus.TO_READ));
        stats.put("reading", bookRepository.countByUserIdAndStatus(userId, ReadingStatus.READING));
        stats.put("completed", bookRepository.countByUserIdAndStatus(userId, ReadingStatus.COMPLETED));
        stats.put("onHold", bookRepository.countByUserIdAndStatus(userId, ReadingStatus.ON_HOLD));
        stats.put("dropped", bookRepository.countByUserIdAndStatus(userId, ReadingStatus.DROPPED));
        stats.put("libraryValue", bookRepository.calculateLibraryValueForUser(userId));

        // Calculate completion rate
        long total = (Long) stats.get("totalBooks");
        long completed = (Long) stats.get("completed");
        double completionRate = total > 0 ? (completed * 100.0 / total) : 0;
        stats.put("completionRate", String.format("%.1f%%", completionRate));

        return stats;
    }

    private List<BookResponse> getRecentBooks(Long userId, int limit) {
        return bookRepository.findRecentBooksForUser(userId, PageRequest.of(0, limit)).stream()
                .map(BookResponse::fromEntity)
                .collect(Collectors.toList());
    }

    private List<BookResponse> getRecommendationsByGenre(Long userId, Genre genre, int limit) {
        // If no genre specified, find user's favorite genre
        if (genre == null) {
            List<Object[]> genreStats = bookRepository.countBooksByGenreForUser(userId);
            if (!genreStats.isEmpty()) {
                genre = (Genre) genreStats.get(0)[0];
            } else {
                genre = Genre.FICTION; // Default
            }
        }

        // Find popular books in this genre that the user doesn't own
        return bookRepository.findPopularBooksInGenreNotOwnedByUser(userId, genre, PageRequest.of(0, limit))
                .stream()
                .map(BookResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ========== Answer Formatting Methods ==========

    private String formatTopReadersAnswer(List<Map<String, Object>> data) {
        if (data.isEmpty()) {
            return "No users have added books yet.";
        }

        StringBuilder sb = new StringBuilder("Here are the top readers:\n");
        for (int i = 0; i < data.size(); i++) {
            Map<String, Object> reader = data.get(i);
            sb.append(String.format("%d. %s with %d books\n",
                    i + 1, reader.get("userName"), reader.get("bookCount")));
        }
        return sb.toString().trim();
    }

    private String formatPopularBooksAnswer(List<Map<String, Object>> data) {
        if (data.isEmpty()) {
            return "No books have been added yet.";
        }

        StringBuilder sb = new StringBuilder("Here are the most popular books (owned by multiple users):\n");
        for (int i = 0; i < data.size(); i++) {
            Map<String, Object> book = data.get(i);
            sb.append(String.format("%d. \"%s\" by %s (owned by %d users)\n",
                    i + 1, book.get("title"), book.get("author"), book.get("ownerCount")));
        }
        return sb.toString().trim();
    }

    private String formatExpensiveBooksAnswer(List<BookResponse> data) {
        if (data.isEmpty()) {
            return "No books with prices have been added yet.";
        }

        StringBuilder sb = new StringBuilder("Here are the most expensive books:\n");
        for (int i = 0; i < data.size(); i++) {
            BookResponse book = data.get(i);
            sb.append(String.format("%d. \"%s\" by %s - $%.2f\n",
                    i + 1, book.getTitle(), book.getAuthor(),
                    book.getPrice() != null ? book.getPrice() : BigDecimal.ZERO));
        }
        return sb.toString().trim();
    }

    private String formatTopAuthorsAnswer(List<Map<String, Object>> data) {
        if (data.isEmpty()) {
            return "No books have been added yet.";
        }

        StringBuilder sb = new StringBuilder("Here are the most popular authors:\n");
        for (int i = 0; i < data.size(); i++) {
            Map<String, Object> author = data.get(i);
            sb.append(String.format("%d. %s (%d books)\n",
                    i + 1, author.get("author"), author.get("bookCount")));
        }
        return sb.toString().trim();
    }

    private String formatGenreDistributionAnswer(Map<String, Long> data) {
        if (data.isEmpty()) {
            return "No books have been added yet.";
        }

        StringBuilder sb = new StringBuilder("Here's the genre distribution:\n");
        data.forEach((genre, count) ->
                sb.append(String.format("• %s: %d books\n", genre, count)));
        return sb.toString().trim();
    }

    private String formatStatusDistributionAnswer(Map<String, Long> data) {
        if (data.isEmpty()) {
            return "No books have been added yet.";
        }

        StringBuilder sb = new StringBuilder("Here's the reading status breakdown:\n");
        data.forEach((status, count) ->
                sb.append(String.format("• %s: %d books\n", status, count)));
        return sb.toString().trim();
    }

    private String formatUserGenreDistributionAnswer(Map<String, Long> data) {
        if (data.isEmpty()) {
            return "You haven't added any books yet.";
        }

        String topGenre = data.keySet().iterator().next();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Your favorite genre is %s! Here's your breakdown:\n", topGenre));
        data.forEach((genre, count) ->
                sb.append(String.format("• %s: %d books\n", genre, count)));
        return sb.toString().trim();
    }

    private String formatUserReadingStatsAnswer(Map<String, Object> data) {
        return String.format("""
                Here are your reading statistics:
                • Total books: %d
                • To read: %d
                • Currently reading: %d
                • Completed: %d
                • On hold: %d
                • Dropped: %d
                • Library value: $%.2f
                • Completion rate: %s""",
                data.get("totalBooks"),
                data.get("toRead"),
                data.get("reading"),
                data.get("completed"),
                data.get("onHold"),
                data.get("dropped"),
                data.get("libraryValue"),
                data.get("completionRate"));
    }

    private String formatRecommendationsAnswer(List<BookResponse> data, Genre genre) {
        if (data.isEmpty()) {
            return genre != null
                    ? String.format("No %s books found to recommend. Try adding more variety!", genre)
                    : "No recommendations available yet. Add more books to get personalized suggestions!";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(genre != null
                ? String.format("Based on your interest in %s, you might enjoy:\n", genre)
                : "Here are some books you might enjoy:\n");
        for (int i = 0; i < data.size(); i++) {
            BookResponse book = data.get(i);
            sb.append(String.format("%d. \"%s\" by %s\n", i + 1, book.getTitle(), book.getAuthor()));
        }
        return sb.toString().trim();
    }

    // ========== Insights Generation ==========

    /**
     * Generate automatic insights (for admin dashboard)
     */
    public InsightResponse generateInsights() {
        log.info("Generating system insights");
        List<String> insights = new ArrayList<>();

        // Total stats
        long totalBooks = bookRepository.count();
        long totalUsers = userRepository.count();
        insights.add(String.format("The library contains %d books across %d users.", totalBooks, totalUsers));

        // Average books per user
        if (totalUsers > 0) {
            double avgBooks = (double) totalBooks / totalUsers;
            insights.add(String.format("Users have an average of %.1f books each.", avgBooks));
        }

        // Top genre
        List<Object[]> genreStats = bookRepository.countBooksByGenre();
        if (!genreStats.isEmpty()) {
            String topGenre = genreStats.get(0)[0].toString();
            Long topGenreCount = (Long) genreStats.get(0)[1];
            insights.add(String.format("%s is the most popular genre with %d books.", topGenre, topGenreCount));
        }

        // Reading completion
        List<Object[]> statusStats = bookRepository.countBooksByStatus();
        long completed = 0, total = 0;
        for (Object[] row : statusStats) {
            long count = (Long) row[1];
            total += count;
            if (row[0] == ReadingStatus.COMPLETED) {
                completed = count;
            }
        }
        if (total > 0) {
            double completionRate = (completed * 100.0 / total);
            insights.add(String.format("%.1f%% of all books have been marked as completed.", completionRate));
        }

        // Top reader
        List<Object[]> topReaders = bookRepository.countBooksPerUser();
        if (!topReaders.isEmpty()) {
            String topReader = (String) topReaders.get(0)[1];
            Long bookCount = (Long) topReaders.get(0)[2];
            insights.add(String.format("%s is the most active reader with %d books.", topReader, bookCount));
        }

        // Most popular author
        List<Object[]> topAuthors = bookRepository.findTopAuthors(PageRequest.of(0, 1));
        if (!topAuthors.isEmpty()) {
            String author = (String) topAuthors.get(0)[0];
            Long count = (Long) topAuthors.get(0)[1];
            insights.add(String.format("%s is the most popular author with %d books in the system.", author, count));
        }

        return InsightResponse.builder()
                .insights(insights)
                .generatedBy("RULE_BASED")
                .generatedAtMs(Instant.now().toEpochMilli())
                .build();
    }

    /**
     * Get query suggestions
     */
    public List<String> getSuggestions() {
        return ruleBasedParser.getSuggestions();
    }
}
