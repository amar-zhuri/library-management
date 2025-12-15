package com.library.library_management.repository.specification;

import com.library.library_management.dto.request.BookSearchRequest;
import com.library.library_management.entity.Book;
import com.library.library_management.entity.enums.Genre;
import com.library.library_management.entity.enums.ReadingStatus;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BookSpecification {

    private BookSpecification() {
        // Private constructor to prevent instantiation
    }

    /**
     * Build a specification from search request for a specific user
     */
    public static Specification<Book> buildSpecification(BookSearchRequest request, Long userId) {
        return (Root<Book> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always filter by user (security)
            if (userId != null) {
                predicates.add(cb.equal(root.get("user").get("id"), userId));
            }

            // Full-text search across multiple fields
            if (request.hasFullTextQuery()) {
                String searchTerm = "%" + request.getQuery().toLowerCase() + "%";
                Predicate titleMatch = cb.like(cb.lower(root.get("title")), searchTerm);
                Predicate authorMatch = cb.like(cb.lower(root.get("author")), searchTerm);
                Predicate descriptionMatch = cb.like(cb.lower(root.get("description")), searchTerm);
                Predicate isbnMatch = cb.like(cb.lower(root.get("isbn")), searchTerm);

                predicates.add(cb.or(titleMatch, authorMatch, descriptionMatch, isbnMatch));
            }

            // Individual field searches
            if (request.getTitle() != null && !request.getTitle().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("title")),
                        "%" + request.getTitle().toLowerCase() + "%"
                ));
            }

            if (request.getAuthor() != null && !request.getAuthor().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("author")),
                        "%" + request.getAuthor().toLowerCase() + "%"
                ));
            }

            if (request.getIsbn() != null && !request.getIsbn().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("isbn")),
                        "%" + request.getIsbn().toLowerCase() + "%"
                ));
            }

            // Genre filter
            if (request.getGenre() != null) {
                predicates.add(cb.equal(root.get("genre"), request.getGenre()));
            }

            // Status filter
            if (request.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }

            // Price range filter
            if (request.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), request.getMinPrice()));
            }

            if (request.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), request.getMaxPrice()));
            }

            // Publication year range filter
            if (request.getMinYear() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("publicationYear"), request.getMinYear()));
            }

            if (request.getMaxYear() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("publicationYear"), request.getMaxYear()));
            }

            // Page count range filter
            if (request.getMinPages() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("pageCount"), request.getMinPages()));
            }

            if (request.getMaxPages() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("pageCount"), request.getMaxPages()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Build specification for admin (no user filter)
     */
    public static Specification<Book> buildAdminSpecification(BookSearchRequest request) {
        return buildSpecification(request, null);
    }

    // ========== Individual Specifications (for composition) ==========

    public static Specification<Book> hasUserId(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Book> hasGenre(Genre genre) {
        return (root, query, cb) -> genre == null ? null : cb.equal(root.get("genre"), genre);
    }

    public static Specification<Book> hasStatus(ReadingStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Book> titleContains(String title) {
        return (root, query, cb) -> {
            if (title == null || title.isBlank()) return null;
            return cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
        };
    }

    public static Specification<Book> authorContains(String author) {
        return (root, query, cb) -> {
            if (author == null || author.isBlank()) return null;
            return cb.like(cb.lower(root.get("author")), "%" + author.toLowerCase() + "%");
        };
    }

    public static Specification<Book> priceBetween(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return null;
            if (min != null && max != null) {
                return cb.between(root.get("price"), min, max);
            } else if (min != null) {
                return cb.greaterThanOrEqualTo(root.get("price"), min);
            } else {
                return cb.lessThanOrEqualTo(root.get("price"), max);
            }
        };
    }

    public static Specification<Book> yearBetween(Integer min, Integer max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return null;
            if (min != null && max != null) {
                return cb.between(root.get("publicationYear"), min, max);
            } else if (min != null) {
                return cb.greaterThanOrEqualTo(root.get("publicationYear"), min);
            } else {
                return cb.lessThanOrEqualTo(root.get("publicationYear"), max);
            }
        };
    }
}