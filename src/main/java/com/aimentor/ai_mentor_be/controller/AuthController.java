package com.aimentor.ai_mentor_be.controller;

import com.aimentor.ai_mentor_be.dto.AuthResponse;
import com.aimentor.ai_mentor_be.dto.GoogleLoginRequest;
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
}