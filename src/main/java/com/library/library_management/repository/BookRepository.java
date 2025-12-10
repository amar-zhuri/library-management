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

}