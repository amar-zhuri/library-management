package com.library.library_management.dto.response;

import com.library.library_management.entity.enums.Genre;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponse {

    private List<RecommendedBook> byGenre;
    private List<RecommendedBook> byAuthor;
    private List<RecommendedBook> fromSimilarUsers;
    private String message;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendedBook {
        private Long id;
        private String title;
        private String author;
        private Genre genre;
        private Integer pageCount;
        private Integer publicationYear;
        private String reason;
    }
}