package com.library.library_management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatsResponse {

    private long totalUsers;
    private long totalBooks;
    private Map<String, Long> booksByGenre;
    private Map<String, Long> booksByStatus;
    private List<TopReaderDto> topReaders;
    private List<PopularBookDto> popularBooks;
    private List<TopAuthorDto> topAuthors;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopReaderDto {
        private Long userId;
        private String userName;
        private Long bookCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PopularBookDto {
        private String title;
        private String author;
        private Long count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopAuthorDto {
        private String author;
        private Long bookCount;
    }
}