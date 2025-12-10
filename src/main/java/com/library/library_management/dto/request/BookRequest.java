package com.library.library_management.dto.request;

import java.math.BigDecimal;

import com.library.library_management.entity.enums.Genre;
import com.library.library_management.entity.enums.ReadingStatus;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookRequest {
   
    @NotBlank(message = "Tittle is requiered")
    @Size(max = 255, message = "Tittle must not exceed 255 characters")
    private String title;

    @NotBlank(message = "Author is required")
    @Size(max = 255, message = "Author must not exceed 255 characters")
    private String author;

    @NotNull(message = "Genre is required")
    private Genre genre;

    private ReadingStatus status;

    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be positive")
    @Digits(integer = 8, fraction = 2, message = "Price format is invalid")
    private BigDecimal price;

    @Size(max = 2000, message = "Descprition mus not exceed 2000 characters")
    private String description;

    @Size(max = 20, message = "ISBN must not exceed 20 characters")
    private String isbn;

    @Min(value = 1, message = "Page count must be at least 1")
    private Integer pageCount;

    @Min(value = 1000, message = "Publication year must be a valid year")
    @Max(value = 2100, message = "Publication year must be a valid year")
    private Integer publicationYear;
}
