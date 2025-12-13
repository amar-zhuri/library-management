package com.library.library_management.service.ai;

import com.library.library_management.entity.enums.Genre;
import com.library.library_management.entity.enums.QueryType;
import com.library.library_management.entity.enums.ReadingStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class RuleBasedQueryParser {

    // Pattern definitions: keywords -> QueryType
    private static final List<QueryPattern> PATTERNS = new ArrayList<>();

    static {
        // Top readers / who owns most books
        PATTERNS.add(new QueryPattern(
                QueryType.TOP_READERS,
                List.of("who", "owns", "most", "books"),
                List.of("top", "readers"),
                List.of("most", "books", "who")
        ));

        // Popular books
        PATTERNS.add(new QueryPattern(
                QueryType.POPULAR_BOOKS,
                List.of("most", "popular", "book"),
                List.of("popular", "books"),
                List.of("commonly", "owned")
        ));

        // Expensive books
        PATTERNS.add(new QueryPattern(
                QueryType.EXPENSIVE_BOOKS,
                List.of("most", "expensive", "books"),
                List.of("expensive", "books"),
                List.of("highest", "price"),
                List.of("costly", "books"),
                List.of("priciest")
        ));

        // Top authors
        PATTERNS.add(new QueryPattern(
                QueryType.TOP_AUTHORS,
                List.of("top", "authors"),
                List.of("most", "common", "author"),
                List.of("popular", "authors"),
                List.of("which", "author", "most"),
                List.of("authors", "appear", "most")
        ));

        // Genre distribution (system-wide)
        PATTERNS.add(new QueryPattern(
                QueryType.GENRE_DISTRIBUTION,
                List.of("genre", "distribution"),
                List.of("books", "by", "genre"),
                List.of("genres", "breakdown"),
                List.of("how", "many", "each", "genre")
        ));

        // Status distribution (system-wide)
        PATTERNS.add(new QueryPattern(
                QueryType.STATUS_DISTRIBUTION,
                List.of("status", "distribution"),
                List.of("reading", "status", "breakdown"),
                List.of("how", "many", "completed"),
                List.of("books", "by", "status")
        ));

        // Total books
        PATTERNS.add(new QueryPattern(
                QueryType.TOTAL_BOOKS,
                List.of("total", "books"),
                List.of("how", "many", "books", "system"),
                List.of("count", "all", "books")
        ));

        // Total users
        PATTERNS.add(new QueryPattern(
                QueryType.TOTAL_USERS,
                List.of("total", "users"),
                List.of("how", "many", "users"),
                List.of("registered", "users"),
                List.of("count", "users")
        ));

        // User book count
        PATTERNS.add(new QueryPattern(
                QueryType.USER_BOOK_COUNT,
                List.of("how", "many", "books", "do", "i"),
                List.of("my", "book", "count"),
                List.of("how", "many", "books", "have", "i"),
                List.of("count", "my", "books")
        ));

        // User genre distribution
        PATTERNS.add(new QueryPattern(
                QueryType.USER_GENRE_DISTRIBUTION,
                List.of("what", "genres", "do", "i", "read"),
                List.of("my", "favorite", "genre"),
                List.of("my", "genre", "distribution"),
                List.of("genres", "i", "read", "most"),
                List.of("my", "genres")
        ));

        // User reading stats
        PATTERNS.add(new QueryPattern(
                QueryType.USER_READING_STATS,
                List.of("my", "reading", "statistics"),
                List.of("my", "reading", "stats"),
                List.of("show", "my", "stats"),
                List.of("my", "progress"),
                List.of("reading", "progress")
        ));

        // User library value
        PATTERNS.add(new QueryPattern(
                QueryType.USER_LIBRARY_VALUE,
                List.of("total", "value", "library"),
                List.of("how", "much", "worth"),
                List.of("library", "worth"),
                List.of("total", "price", "books"),
                List.of("value", "my", "books")
        ));

        // User books by status
        PATTERNS.add(new QueryPattern(
                QueryType.USER_BOOKS_BY_STATUS,
                List.of("books", "i", "completed"),
                List.of("books", "i", "am", "reading"),
                List.of("my", "completed", "books"),
                List.of("books", "to", "read"),
                List.of("unread", "books"),
                List.of("finished", "books")
        ));

        // User recent books
        PATTERNS.add(new QueryPattern(
                QueryType.USER_RECENT_BOOKS,
                List.of("recently", "added"),
                List.of("recent", "books"),
                List.of("latest", "books"),
                List.of("new", "additions")
        ));

        // Recommendations by genre
        PATTERNS.add(new QueryPattern(
                QueryType.RECOMMENDATIONS_BY_GENRE,
                List.of("recommend", "genre"),
                List.of("suggest", "books", "genre"),
                List.of("what", "should", "read", "genre"),
                List.of("books", "like", "genre")
        ));

        // Recommendations by author
        PATTERNS.add(new QueryPattern(
                QueryType.RECOMMENDATIONS_BY_AUTHOR,
                List.of("recommend", "author"),
                List.of("more", "books", "by"),
                List.of("similar", "author"),
                List.of("books", "by", "same", "author")
        ));
    }

    // Pattern for extracting numbers (e.g., "top 5", "show 10")
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\b(\\d+)\\b");

    // Mapping of genre keywords to Genre enum
    private static final Map<String, Genre> GENRE_KEYWORDS = new HashMap<>();

    static {
        GENRE_KEYWORDS.put("fiction", Genre.FICTION);
        GENRE_KEYWORDS.put("non-fiction", Genre.NON_FICTION);
        GENRE_KEYWORDS.put("nonfiction", Genre.NON_FICTION);
        GENRE_KEYWORDS.put("mystery", Genre.MYSTERY);
        GENRE_KEYWORDS.put("sci-fi", Genre.SCIENCE_FICTION);
        GENRE_KEYWORDS.put("scifi", Genre.SCIENCE_FICTION);
        GENRE_KEYWORDS.put("science fiction", Genre.SCIENCE_FICTION);
        GENRE_KEYWORDS.put("fantasy", Genre.FANTASY);
        GENRE_KEYWORDS.put("romance", Genre.ROMANCE);
        GENRE_KEYWORDS.put("thriller", Genre.THRILLER);
        GENRE_KEYWORDS.put("biography", Genre.BIOGRAPHY);
        GENRE_KEYWORDS.put("history", Genre.HISTORY);
        GENRE_KEYWORDS.put("historical", Genre.HISTORY);
        GENRE_KEYWORDS.put("science", Genre.SCIENCE);
        GENRE_KEYWORDS.put("self-help", Genre.SELF_HELP);
        GENRE_KEYWORDS.put("selfhelp", Genre.SELF_HELP);
        GENRE_KEYWORDS.put("self help", Genre.SELF_HELP);
        GENRE_KEYWORDS.put("poetry", Genre.POETRY);
        GENRE_KEYWORDS.put("drama", Genre.DRAMA);
        GENRE_KEYWORDS.put("horror", Genre.HORROR);
        GENRE_KEYWORDS.put("adventure", Genre.ADVENTURE);
        GENRE_KEYWORDS.put("children", Genre.CHILDREN);
        GENRE_KEYWORDS.put("kids", Genre.CHILDREN);
        GENRE_KEYWORDS.put("young adult", Genre.YOUNG_ADULT);
        GENRE_KEYWORDS.put("ya", Genre.YOUNG_ADULT);
        GENRE_KEYWORDS.put("comics", Genre.COMICS);
        GENRE_KEYWORDS.put("comic", Genre.COMICS);
        GENRE_KEYWORDS.put("graphic novel", Genre.COMICS);
        GENRE_KEYWORDS.put("art", Genre.ART);
        GENRE_KEYWORDS.put("cooking", Genre.COOKING);
        GENRE_KEYWORDS.put("cookbook", Genre.COOKING);
        GENRE_KEYWORDS.put("travel", Genre.TRAVEL);
        GENRE_KEYWORDS.put("religion", Genre.RELIGION);
        GENRE_KEYWORDS.put("religious", Genre.RELIGION);
        GENRE_KEYWORDS.put("philosophy", Genre.PHILOSOPHY);
        GENRE_KEYWORDS.put("psychology", Genre.PSYCHOLOGY);
        GENRE_KEYWORDS.put("business", Genre.BUSINESS);
        GENRE_KEYWORDS.put("technology", Genre.TECHNOLOGY);
        GENRE_KEYWORDS.put("tech", Genre.TECHNOLOGY);
        GENRE_KEYWORDS.put("programming", Genre.TECHNOLOGY);
    }

    // Mapping of status keywords to ReadingStatus enum
    private static final Map<String, ReadingStatus> STATUS_KEYWORDS = new HashMap<>();

    static {
        STATUS_KEYWORDS.put("to read", ReadingStatus.TO_READ);
        STATUS_KEYWORDS.put("to-read", ReadingStatus.TO_READ);
        STATUS_KEYWORDS.put("unread", ReadingStatus.TO_READ);
        STATUS_KEYWORDS.put("not started", ReadingStatus.TO_READ);
        STATUS_KEYWORDS.put("reading", ReadingStatus.READING);
        STATUS_KEYWORDS.put("currently reading", ReadingStatus.READING);
        STATUS_KEYWORDS.put("in progress", ReadingStatus.READING);
        STATUS_KEYWORDS.put("completed", ReadingStatus.COMPLETED);
        STATUS_KEYWORDS.put("finished", ReadingStatus.COMPLETED);
        STATUS_KEYWORDS.put("done", ReadingStatus.COMPLETED);
        STATUS_KEYWORDS.put("read", ReadingStatus.COMPLETED);
        STATUS_KEYWORDS.put("on hold", ReadingStatus.ON_HOLD);
        STATUS_KEYWORDS.put("paused", ReadingStatus.ON_HOLD);
        STATUS_KEYWORDS.put("dropped", ReadingStatus.DROPPED);
        STATUS_KEYWORDS.put("abandoned", ReadingStatus.DROPPED);
        STATUS_KEYWORDS.put("quit", ReadingStatus.DROPPED);
    }

    /**
     * Parse a natural language question into a QueryIntent
     */
    public QueryIntent parse(String question) {
        if (question == null || question.isBlank()) {
            return QueryIntent.unknown("");
        }

        String normalized = normalize(question);
        log.debug("Parsing question: '{}' -> normalized: '{}'", question, normalized);

        // Try to match against patterns
        QueryPattern bestMatch = null;
        int bestScore = 0;

        for (QueryPattern pattern : PATTERNS) {
            int score = pattern.match(normalized);
            if (score > bestScore) {
                bestScore = score;
                bestMatch = pattern;
            }
        }

        if (bestMatch != null && bestScore >= 2) {
            log.info("Matched query type: {} with score: {}", bestMatch.getQueryType(), bestScore);

            return QueryIntent.builder()
                    .queryType(bestMatch.getQueryType())
                    .limit(extractLimit(normalized))
                    .genre(extractGenre(normalized))
                    .status(extractStatus(normalized))
                    .originalQuestion(question)
                    .confidence(Math.min(1.0, bestScore / 4.0))
                    .build();
        }

        log.info("No pattern matched for question: '{}'", question);
        return QueryIntent.unknown(question);
    }

    /**
     * Normalize the question for matching
     */
    private String normalize(String question) {
        return question.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", " ")  // Remove punctuation except hyphen
                .replaceAll("\\s+", " ")            // Collapse multiple spaces
                .trim();
    }

    /**
     * Extract a number from the question (for "top N" queries)
     */
    private Integer extractLimit(String normalized) {
        Matcher matcher = NUMBER_PATTERN.matcher(normalized);
        if (matcher.find()) {
            try {
                int num = Integer.parseInt(matcher.group(1));
                // Reasonable limits
                if (num > 0 && num <= 100) {
                    return num;
                }
            } catch (NumberFormatException e) {
                // Ignore
            }
        }
        return null; // Default will be applied by service
    }

    /**
     * Extract genre from the question
     */
    private Genre extractGenre(String normalized) {
        for (Map.Entry<String, Genre> entry : GENRE_KEYWORDS.entrySet()) {
            if (normalized.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Extract reading status from the question
     */
    private ReadingStatus extractStatus(String normalized) {
        for (Map.Entry<String, ReadingStatus> entry : STATUS_KEYWORDS.entrySet()) {
            if (normalized.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Get suggested questions for users
     */
    public List<String> getSuggestions() {
        List<String> suggestions = new ArrayList<>();
        for (QueryType type : QueryType.values()) {
            if (type != QueryType.UNKNOWN) {
                suggestions.add(type.getExampleQuestion());
            }
        }
        return suggestions;
    }

    /**
     * Inner class representing a query pattern
     */
    private static class QueryPattern {
        private final QueryType queryType;
        private final List<List<String>> keywordSets;

        @SafeVarargs
        QueryPattern(QueryType queryType, List<String>... keywordSets) {
            this.queryType = queryType;
            this.keywordSets = Arrays.asList(keywordSets);
        }

        QueryType getQueryType() {
            return queryType;
        }

        /**
         * Calculate match score (higher = better match)
         */
        int match(String normalized) {
            int bestScore = 0;

            for (List<String> keywords : keywordSets) {
                int score = 0;
                for (String keyword : keywords) {
                    if (normalized.contains(keyword)) {
                        score++;
                    }
                }
                // All keywords must match for this set
                if (score == keywords.size()) {
                    bestScore = Math.max(bestScore, score);
                }
            }

            return bestScore;
        }
    }
}