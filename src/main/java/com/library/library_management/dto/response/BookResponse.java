package com.library.library_management.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.library.library_management.entity.Book;

import com.library.library_management.entity.enums.Genre;
import com.library.library_management.entity.enums.ReadingStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    // Static factory method to convert Entity to DTO
    public static BookResponse fromEntity(Book book)
    {
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
}
