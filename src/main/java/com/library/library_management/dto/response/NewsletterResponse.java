package com.library.library_management.dto.response;

import com.library.library_management.entity.Newsletter;
import com.library.library_management.entity.enums.NewsletterStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsletterResponse {

    private Long id;
    private String subject;
    private String content;
    private NewsletterStatus status;
    private String createdByName;
    private LocalDateTime sentAt;
    private Integer recipientCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static NewsletterResponse fromEntity(Newsletter newsletter) {
        return NewsletterResponse.builder()
                .id(newsletter.getId())
                .subject(newsletter.getSubject())
                .content(newsletter.getContent())
                .status(newsletter.getStatus())
                .createdByName(newsletter.getCreatedBy() != null ? newsletter.getCreatedBy().getName() : null)
                .sentAt(newsletter.getSentAt())
                .recipientCount(newsletter.getRecipientCount())
                .createdAt(newsletter.getCreatedAt())
                .updatedAt(newsletter.getUpdatedAt())
                .build();
    }
}