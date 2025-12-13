package com.library.library_management.entity.enums;

public enum QueryType {
    
    USER_BOOK_COUNT("How many books do I have?"),
    USER_BOOKS_BY_STATUS("Show my books by reading status"),
    USER_BOOKS_BY_GENRE("Show my books by genre"),
    USER_GENRE_DISTRIBUTION("What genres do I read most?"),
    USER_READING_STATS("Show my reading statistics"),
    USER_LIBRARY_VALUE("What's the total value of my library?"),
    USER_RECENT_BOOKS("Show my recently added books"),
    USER_BOOKS_BY_AUTHOR("Show books by a specific author"),
    
    // System-wide queries (Admin or aggregated)
    TOP_READERS("Who owns the most books?"),
    POPULAR_BOOKS("Which is the most popular book?"),
    EXPENSIVE_BOOKS("Show the most expensive books"),
    TOP_AUTHORS("Which authors appear most?"),
    GENRE_DISTRIBUTION("What's the genre distribution?"),
    STATUS_DISTRIBUTION("What's the reading status distribution?"),
    TOTAL_BOOKS("How many total books are in the system?"),
    TOTAL_USERS("How many users are registered?"),

    RECOMMENDATIONS_BY_GENRE("Recommend books based on my favorite genre"),
    RECOMMENDATIONS_BY_AUTHOR("Recommend books by authors I like"),
    
    UNKNOWN("Query not recognized");

    private final String exampleQuestion;

    QueryType(String exampleQuestion) {
        this.exampleQuestion = exampleQuestion;
    }

    public String getExampleQuestion() {
        return exampleQuestion;
    }
}