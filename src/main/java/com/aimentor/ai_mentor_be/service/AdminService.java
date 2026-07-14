package com.aimentor.ai_mentor_be.service;

import com.aimentor.ai_mentor_be.dto.StatisticsResponse;
import com.aimentor.ai_mentor_be.dto.UserAdminResponse;
import com.aimentor.ai_mentor_be.entity.User;
import com.aimentor.ai_mentor_be.repository.DocumentRepository;
import com.aimentor.ai_mentor_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;

    private final DocumentRepository documentRepository;

    /**
     * API 1
     * Thống kê số tài khoản đã từng đăng nhập
     */
    public StatisticsResponse getTotalLoggedUsers() {

        return StatisticsResponse.builder()
                .total(userRepository.countByLastLoginIsNotNull())
                .build();
    }

    /**
     * API 2
     * Thống kê số tài liệu hiện có trên hệ thống
     */
    public StatisticsResponse getTotalDocuments() {

        return StatisticsResponse.builder()
                .total(documentRepository.count())
                .build();
    }

    /**
     * API 3
     * Lấy danh sách người dùng mới theo số ngày
     */
    public List<UserAdminResponse> getNewUsers(int days) {

        if (days != 1 && days != 7) {
            throw new RuntimeException("Days chỉ được phép là 1 hoặc 7");
        }

        Timestamp from = Timestamp.valueOf(
                LocalDateTime.now().minusDays(days)
        );

        return userRepository.findByCreatedAtAfter(from)
                .stream()
                .map(user -> UserAdminResponse.builder()
                        .userId(user.getUserId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .role(user.getRole().getRoleName())
                        .isActive(user.getIsActive())
                        .createdAt(user.getCreatedAt())
                        .lastLogin(user.getLastLogin())
                        .build())
                .toList();
    }

    /**
     * Khóa tài khoản người dùng
     */
    public void lockUser(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        // Không cho khóa tài khoản ADMIN
        if ("ADMIN".equals(user.getRole().getRoleName())) {
            throw new RuntimeException("Cannot lock admin account");
        }

        user.setIsActive(false);

        userRepository.save(user);
    }

    /**
     * Mở khóa tài khoản người dùng
     */
    public void unlockUser(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        user.setIsActive(true);

        userRepository.save(user);
    }
}