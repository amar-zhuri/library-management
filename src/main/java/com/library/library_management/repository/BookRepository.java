package com.library.library_management.repository;

import com.library.library_management.entity.Book;
import com.library.library_management.entity.enums.Genre;
import com.library.library_management.entity.enums.ReadingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
@Repository
public interface BookRepository extends JpaRepository<Book, Long>{

        // Find all books for a specific user
    List<Book> findByUserId(Long userId);

    // Find all books for a user with pagination
    Page<Book> findByUserId(Long userId, Pageable pageable);
    
    // Find a specific book by ID and user ID (security check)
    Optional<Book> findByIdAndUserId(Long id, Long userId);
    
    // Find books by user and genre
    Page<Book> findByUserIdAndGenre(Long userId, Genre genre, Pageable pageable);
    
    // Find books by user and status
    Page<Book> findByUserIdAndStatus(Long userId, ReadingStatus status, Pageable pageable);
     // Find books by user, genre, and status
    Page<Book> findByUserIdAndGenreAndStatus(Long userId, Genre genre, ReadingStatus status, Pageable pageable);
    
    // Search by title (case-insensitive, partial match)
    Page<Book> findByUserIdAndTitleContainingIgnoreCase(Long userId, String title, Pageable pageable);
    
    // Search by author (case-insensitive, partial match)
    Page<Book> findByUserIdAndAuthorContainingIgnoreCase(Long userId, String author, Pageable pageable);
    
    // Count books by user
    long countByUserId(Long userId);
    
    // Count books by user and status
    long countByUserIdAndStatus(Long userId, ReadingStatus status);

    // ========== Queries for AI/Analytics (Admin) ==========
    
    // Count books per user (for "who owns the most books")
    @Query("SELECT b.user.id, b.user.name, COUNT(b) as bookCount " +
           "FROM Book b GROUP BY b.user.id, b.user.name ORDER BY bookCount DESC")
    List<Object[]> countBooksPerUser();


    
    
    // Most popular books by title (across all users)
    @Query("SELECT b.title, b.author, COUNT(b) as count " +
           "FROM Book b GROUP BY b.title, b.author ORDER BY count DESC")
    List<Object[]> findMostPopularBooks(Pageable pageable);
    
    // Most expensive books
        //@Query("SELECT b FROM Book b WHERE b.price IS NOT NULL ORDER BY b.price DESC")
       // List<Book> findMostExpensiveBooks(Pageable pageable);


        @Query("SELECT b FROM Book b JOIN FETCH b.user WHERE b.price IS NOT NULL ORDER BY b.price DESC")
        List<Book> findMostExpensiveBooks(Pageable pageable);



    List<Book> findTop5ByPriceIsNotNullOrderByPriceDesc();
    
    // Books by genre distribution
    @Query("SELECT b.genre, COUNT(b) FROM Book b GROUP BY b.genre ORDER BY COUNT(b) DESC")
    List<Object[]> countBooksByGenre();
    
    // Books by status distribution
    @Query("SELECT b.status, COUNT(b) FROM Book b GROUP BY b.status")
    List<Object[]> countBooksByStatus();

     // Top authors (most books in the system)
    @Query("SELECT b.author, COUNT(b) as count FROM Book b GROUP BY b.author ORDER BY count DESC")
    List<Object[]> findTopAuthors(Pageable pageable);
    
     // ========== User-specific AI Query Methods ==========

    // User's genre distribution
    @Query("SELECT b.genre, COUNT(b) FROM Book b WHERE b.user.id = :userId GROUP BY b.genre ORDER BY COUNT(b) DESC")
    List<Object[]> countBooksByGenreForUser(@Param("userId") Long userId);

    // User's status distribution
    @Query("SELECT b.status, COUNT(b) FROM Book b WHERE b.user.id = :userId GROUP BY b.status")
    List<Object[]> countBooksByStatusForUser(@Param("userId") Long userId);

    // User's total library value
    @Query("SELECT COALESCE(SUM(b.price), 0) FROM Book b WHERE b.user.id = :userId")
    BigDecimal calculateLibraryValueForUser(@Param("userId") Long userId);

    // User's top authors
    @Query("SELECT b.author, COUNT(b) as count FROM Book b WHERE b.user.id = :userId GROUP BY b.author ORDER BY count DESC")
    List<Object[]> findTopAuthorsForUser(@Param("userId") Long userId, Pageable pageable);

    // User's recent books
    @Query("SELECT b FROM Book b WHERE b.user.id = :userId ORDER BY b.createdAt DESC")
    List<Book> findRecentBooksForUser(@Param("userId") Long userId, Pageable pageable);


    // User's books by status
    List<Book> findByUserIdAndStatus(Long userId, ReadingStatus status);

    // User's books by genre
    List<Book> findByUserIdAndGenre(Long userId, Genre genre);

    // Find books by author for a user
    List<Book> findByUserIdAndAuthorContainingIgnoreCase(Long userId, String author);

    // Count books by genre for user
    long countByUserIdAndGenre(Long userId, Genre genre);

    // Find other users who have similar books (for collaborative filtering)
    @Query("SELECT DISTINCT b2.user.id FROM Book b1 " +
            "JOIN Book b2 ON b1.title = b2.title AND b1.author = b2.author " +
            "WHERE b1.user.id = :userId AND b2.user.id != :userId")
    List<Long> findUsersWithSimilarBooks(@Param("userId") Long userId);

    // Find books that similar users have but current user doesn't
    @Query("SELECT b FROM Book b WHERE b.user.id IN :similarUserIds " +
            "AND NOT EXISTS (SELECT 1 FROM Book ub WHERE ub.user.id = :userId " +
            "AND ub.title = b.title AND ub.author = b.author)")
    List<Book> findBooksFromSimilarUsers(@Param("userId") Long userId,
                                          @Param("similarUserIds") List<Long> similarUserIds,
                                          Pageable pageable);

        // Find books by user's favorite genre that they don't have
    @Query("SELECT b FROM Book b WHERE b.genre = :genre " +
            "AND NOT EXISTS (SELECT 1 FROM Book ub WHERE ub.user.id = :userId " +
            "AND ub.title = b.title AND ub.author = b.author) " +
            "GROUP BY b.id ORDER BY COUNT(b) DESC")
    List<Book> findPopularBooksInGenreNotOwnedByUser(@Param("userId") Long userId,
                                                      @Param("genre") Genre genre,
                                                      Pageable pageable);


}
