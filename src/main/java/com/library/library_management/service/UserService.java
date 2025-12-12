package com.library.library_management.service;

import com.library.library_management.dto.response.PagedResponse;
import com.library.library_management.dto.response.UserDetailResponse;
import com.library.library_management.dto.response.UserResponse;
import com.library.library_management.entity.User;
import com.library.library_management.exception.ResourceNotFoundException;
import com.library.library_management.exception.UnauthorizedException;
import com.library.library_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    /**
     * Get all users with pagination (Admin only)
     */
    public PagedResponse<UserResponse> getAllUsers(Pageable pageable) {
        log.info("Fetching all users");

        Page<User> userPage = userRepository.findAll(pageable);
        Page<UserResponse> responsePage = userPage.map(UserResponse::fromEntity);

        return PagedResponse.fromPage(responsePage);
    }

    /**
     * Get user by ID with their books (Admin only)
     */
    public UserDetailResponse getUserById(Long userId) {
        log.info("Fetching user with id: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        return UserDetailResponse.fromEntity(user);
    }

    /**
     * Delete a user and all their books (Admin only)
     */
    @Transactional
    public void deleteUser(Long userId, Long adminId) {
        log.info("Admin {} deleting user {}", adminId, userId);

        // Prevent admin from deleting themselves
        if (userId.equals(adminId)) {
            throw new UnauthorizedException("Cannot delete your own account");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        userRepository.delete(user);
        log.info("User {} deleted successfully", userId);
    }

    /**
     * Get total user count
     */
    public long getUserCount() {
        return userRepository.count();
    }
}