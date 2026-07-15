package com.aimentor.ai_mentor_be.controller;

import com.aimentor.ai_mentor_be.dto.StatisticsResponse;
import com.aimentor.ai_mentor_be.dto.UserAdminResponse;
import com.aimentor.ai_mentor_be.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.aimentor.ai_mentor_be.dto.DashboardStatisticsResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * API 1
     * Thống kê số tài khoản đã đăng nhập
     */
    @GetMapping("/statistics/users")
    public StatisticsResponse getTotalLoggedUsers() {

        return adminService.getTotalLoggedUsers();

    }

    /**
     * API 2
     * Thống kê số tài liệu hiện có trên hệ thống
     */
    @GetMapping("/statistics/documents")
    public StatisticsResponse getTotalDocuments() {

        return adminService.getTotalDocuments();

    }

    /**
     * API 3
     * Lấy danh sách người dùng mới
     * days chỉ nhận 1 hoặc 7
     */
    @GetMapping("/users/new")
    public List<UserAdminResponse> getNewUsers(
            @RequestParam int days
    ) {

        return adminService.getNewUsers(days);

    }

    /**
     * Khóa tài khoản
     */
    @PutMapping("/users/{userId}/lock")
    public String lockUser(
            @PathVariable UUID userId
    ) {

        adminService.lockUser(userId);

        return "User locked successfully";

    }

    /**
     * Mở khóa tài khoản
     */
    @PutMapping("/users/{userId}/unlock")
    public String unlockUser(
            @PathVariable UUID userId
    ) {

        adminService.unlockUser(userId);

        return "User unlocked successfully";

    }
    @GetMapping("/users")
    public List<UserAdminResponse> getAllUsers() {

        return adminService.getAllUsers();

    }
    /**
     * Xem chi tiết người dùng
     */
    @GetMapping("/users/{userId}")
    public UserAdminResponse getUserDetail(
            @PathVariable UUID userId
    ) {

        return adminService.getUserDetail(userId);

    }
    @GetMapping("/dashboard")
    public DashboardStatisticsResponse getDashboardStatistics(
            @RequestParam int days
    ) {

        return adminService.getDashboardStatistics(days);

    }


}