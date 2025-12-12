package com.library.library_management.dto.response;

import com.library.library_management.entity.Book;
import com.library.library_management.entity.enums.Genre;
import com.library.library_management.entity.enums.ReadingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookResponse {

    private Long id;
    private String title;
    private String author;
    private Genre genre;
    private ReadingStatus status;
    private BigDecimal price;
    private String description;
    private String isbn;
    private Integer pageCount;
    private Integer publicationYear;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Owner info (populated for admin views)
    private OwnerInfo owner;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OwnerInfo {
        private Long id;
        private String name;
        private String email;
    }

    /**
     * Convert entity to response (without owner info - for user's own books)
     */
    public static BookResponse fromEntity(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .genre(book.getGenre())
                .status(book.getStatus())
                .price(book.getPrice())
                .description(book.getDescription())
                .isbn(book.getIsbn())
                .pageCount(book.getPageCount())
                .publicationYear(book.getPublicationYear())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }

    /**
     * Convert entity to response (with owner info - for admin views)
     */
    public static BookResponse fromEntityWithOwner(Book book) {
        BookResponse response = fromEntity(book);

        if (book.getUser() != null) {
            response.setOwner(OwnerInfo.builder()
                    .id(book.getUser().getId())
                    .name(book.getUser().getName())
                    .email(book.getUser().getEmail())
                    .build());
        }

        return response;
    }
}