package com.library.library_management.dto.response;

import com.library.library_management.entity.User;
import com.library.library_management.entity.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailResponse {

    private Long id;
    private String email;
    private String name;
    private Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<BookResponse> books;

    public static UserDetailResponse fromEntity(User user) {
        return UserDetailResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .books(user.getBooks() != null
                        ? user.getBooks().stream()
                            .map(BookResponse::fromEntity)
                            .collect(Collectors.toList())
                        : List.of())
                .build();
    }
}