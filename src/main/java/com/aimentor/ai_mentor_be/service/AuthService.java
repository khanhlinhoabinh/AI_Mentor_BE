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

import java.sql.Timestamp;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;

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

        String jwt = jwtService.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(jwt)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().getRoleName())
                .build();
    }
}