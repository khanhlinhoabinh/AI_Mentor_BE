package com.aimentor.ai_mentor_be.controller;

import com.aimentor.ai_mentor_be.dto.*;
import com.aimentor.ai_mentor_be.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/google")
    public AuthResponse loginGoogle(
            @RequestBody GoogleLoginRequest request
    ) throws Exception {

        return authService.loginWithGoogle(request.getIdToken());
    }
// đăng nhập admin
    @PostMapping("/admin/login")
    public AuthResponse adminLogin(
            @RequestBody AdminLoginRequest request
    ) {

        return authService.loginAdmin(
                request.getEmail(),
                request.getPassword()
        );
    }
    // đổi mật khẩu
    @PostMapping("/admin/change-password")
    public String changePassword(
            @RequestBody ChangePasswordRequest request
    ) {

        authService.changePassword(
                request.getEmail(),
                request.getOldPassword(),
                request.getNewPassword()
        );

        return "Password changed successfully";
    }
    // quên mật khẩu
    @PostMapping("/forgot-password")
    public String forgotPassword(
            @RequestBody ForgotPasswordRequest request
    ) {

        authService.forgotPassword(
                request.getEmail()
        );

        return "Reset password email sent";
    }
    // reset mật khẩu
    @PostMapping("/reset-password")
    public String resetPassword(
            @RequestBody ResetPasswordRequest request
    ) {

        authService.resetPassword(
                request.getToken(),
                request.getNewPassword()
        );

        return "Password reset successful";
    }
}