package com.aimentor.ai_mentor_be.service;

import com.aimentor.ai_mentor_be.dto.AuthResponse;
import com.aimentor.ai_mentor_be.entity.Role;
import com.aimentor.ai_mentor_be.entity.User;
import com.aimentor.ai_mentor_be.repository.RoleRepository;
import com.aimentor.ai_mentor_be.repository.UserRepository;
import com.aimentor.ai_mentor_be.security.JwtService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.sql.Timestamp;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final ActivityLogService activityLogService;

    @Value("${google.client.id}")
    private String googleClientId;

    public AuthResponse loginWithGoogle(String idTokenString) throws Exception {

        GoogleIdTokenVerifier verifier =
                new GoogleIdTokenVerifier.Builder(
                        new NetHttpTransport(),
                        GsonFactory.getDefaultInstance()
                )
                        .setAudience(
                                Collections.singletonList(googleClientId)
                        )
                        .build();

        GoogleIdToken idToken = verifier.verify(idTokenString);

        if (idToken == null) {
            throw new RuntimeException("Google token is invalid");
        }

        GoogleIdToken.Payload payload = idToken.getPayload();

        String email = payload.getEmail();

        if (email == null) {
            throw new RuntimeException("Email not found from Google");
        }

        String fullName = (String) payload.get("name");
        String googleId = payload.getSubject();
        String avatarUrl = (String) payload.get("picture");

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {

                    Role userRole = roleRepository
                            .findByRoleName("USER")
                            .orElseThrow(() ->
                                    new RuntimeException("Role USER not found")
                            );

                    User newUser = User.builder()
                            .email(email)
                            .fullName(fullName)
                            .googleId(googleId)
                            .avatarUrl(avatarUrl)
                            .isActive(true)
                            .createdAt(
                                    new Timestamp(System.currentTimeMillis())
                            )
                            .role(userRole)
                            .build();

                    return userRepository.save(newUser);
                });

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new RuntimeException("Account has been locked");
        }

        user.setLastLogin(new Timestamp(System.currentTimeMillis()));
        userRepository.save(user);
        activityLogService.saveLog(
                user,
                "LOGIN",
                user.getFullName() + " đăng nhập bằng Google"
        );

        String jwt = jwtService.generateToken(
                user.getEmail(),
                user.getRole().getRoleName()
        );
        return AuthResponse.builder()
                .token(jwt)
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().getRoleName())
                .build();
    }
    public AuthResponse loginAdmin(
            String email,
            String password
    ) {

        String defaultAdminPassword = "123456@aimentor";

        User user = userRepository.findByEmail(email)
                .orElse(null);
        // Nếu tài khoản tồn tại nhưng đã bị khóa
        if (user != null && !Boolean.TRUE.equals(user.getIsActive())) {
            throw new RuntimeException("Account has been locked");
        }

        // nếu chưa tồn tại => tạo admin mới
        if (user == null) {

            if (!password.equals(defaultAdminPassword)) {
                throw new RuntimeException("Wrong default admin password");
            }

            Role adminRole = roleRepository
                    .findByRoleName("ADMIN")
                    .orElseThrow(() ->
                            new RuntimeException("ADMIN role not found")
                    );

            user = User.builder()
                    .email(email)
                    .fullName("Administrator")
                    .passwordHash(
                            passwordEncoder.encode(defaultAdminPassword)
                    )
                    .isActive(true)
                    .createdAt(
                            new Timestamp(System.currentTimeMillis())
                    )
                    .role(adminRole)
                    .build();

            userRepository.save(user);
        }

        // kiểm tra password đã mã hóa
        boolean isMatch = passwordEncoder.matches(
                password,
                user.getPasswordHash()
        );

        if (!isMatch) {
            throw new RuntimeException("Wrong password");
        }
        user.setLastLogin(new Timestamp(System.currentTimeMillis()));

        userRepository.save(user);
        activityLogService.saveLog(
                user,
                "LOGIN",
                user.getFullName() + " đăng nhập hệ thống"
        );

        String jwt = jwtService.generateToken(
                user.getEmail(),
                user.getRole().getRoleName()
        );
        return AuthResponse.builder()
                .token(jwt)
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().getRoleName())
                .build();
    }
    public void changePassword(
            String email,
            String oldPassword,
            String newPassword
    ) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        boolean isMatch = passwordEncoder.matches(
                oldPassword,
                user.getPasswordHash()
        );

        if (!isMatch) {
            throw new RuntimeException("Old password incorrect");
        }

        user.setPasswordHash(
                passwordEncoder.encode(newPassword)
        );

        userRepository.save(user);
    }
    public void forgotPassword(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        String token = java.util.UUID.randomUUID().toString();

        user.setResetToken(token);

        user.setResetTokenExpiry(
                new Timestamp(
                        System.currentTimeMillis()
                                + 15 * 60 * 1000
                )
        );

        userRepository.save(user);

        String resetLink =
                "http://localhost:5173/reset-password?token="
                        + token;

        emailService.sendResetPasswordEmail(
                email,
                resetLink
        );
    }
    public void resetPassword(
            String token,
            String newPassword
    ) {

        User user = userRepository.findByResetToken(token)
                .orElseThrow(() ->
                        new RuntimeException("Invalid token")
                );

        if (user.getResetTokenExpiry()
                .before(new Timestamp(System.currentTimeMillis()))) {

            throw new RuntimeException("Token expired");
        }

        user.setPasswordHash(
                passwordEncoder.encode(newPassword)
        );

        user.setResetToken(null);
        user.setResetTokenExpiry(null);

        userRepository.save(user);
    }
}