package com.library.library_management.repository;

import com.library.library_management.entity.Book;
import com.library.library_management.entity.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GlobalBookRepository extends JpaRepository<Book, Long> {

    /**
     * Books owned by admins (system library).
     */
    @Query("SELECT b FROM Book b WHERE b.user.role = :role")
    Page<Book> findAllByOwnerRole(@Param("role") Role role, Pageable pageable);

    /**
     * Single admin-owned book.
     */
    @Query("SELECT b FROM Book b WHERE b.id = :id AND b.user.role = :role")
    Optional<Book> findByIdAndOwnerRole(@Param("id") Long id, @Param("role") Role role);
}
