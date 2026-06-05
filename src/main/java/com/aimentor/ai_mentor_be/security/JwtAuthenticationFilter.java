package com.aimentor.ai_mentor_be.security;
import com.aimentor.ai_mentor_be.entity.User;
import com.aimentor.ai_mentor_be.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private final JwtService jwtService;

    private final UserRepository userRepository;


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        System.out.println("JWT FILTER RUNNING");

        String authHeader =
                request.getHeader("Authorization");

        if (
                authHeader == null ||
                        !authHeader.startsWith("Bearer ")
        ) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        String token =
                authHeader.substring(7);

        try {
            SecurityContextHolder.clearContext();

            String email =
                    jwtService.extractEmail(token);
            System.out.println("EMAIL = " + email);
            User user =
                    userRepository
                            .findByEmail(email)
                            .orElse(null);

            System.out.println("USER = " + user);
            if (user != null) {

                System.out.println(
                        "ROLE = " +
                                user.getRole().getRoleName()
                );

                System.out.println(
                        "AUTHORITY = ROLE_" +
                                user.getRole().getRoleName()
                );

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                List.of(
                                        new SimpleGrantedAuthority(
                                                "ROLE_" + user.getRole().getRoleName()
                                        )
                                )
                        );

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(auth);

                System.out.println(
                        SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                );
            }

        } catch (Exception e) {

            System.out.println("JWT ERROR = " + e.getMessage());

            e.printStackTrace();

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            return;
        }

        filterChain.doFilter(
                request,
                response
        );
    }
}