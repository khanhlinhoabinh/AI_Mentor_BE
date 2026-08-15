package com.aimentor.ai_mentor_be.config;

import com.aimentor.ai_mentor_be.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth

                        // ================= PUBLIC =================

                        .requestMatchers("/api/auth/**").permitAll()

                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ================= ADMIN =================

                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/subjects/*/documents/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/documents/**").hasRole("ADMIN")

                        // ================= AUTHENTICATED =================

                        .requestMatchers("/api/quiz/**").authenticated()

                        .requestMatchers("/api/streaks/**").authenticated()

                        // ✅ MỚI — tính năng đánh giá học tập AI
                        .requestMatchers("/api/learning-activity/**").hasRole("USER")
                        .requestMatchers("/api/learning-evaluation/**").hasRole("USER")

                        .requestMatchers("/api/chat/**").authenticated()

                        .requestMatchers("/api/reminders/**").authenticated()

                        .requestMatchers("/api/notifications/**").authenticated()

                        .requestMatchers("/api/subjects/*/documents/*/file")
                        .authenticated()

                        .requestMatchers("/api/subjects/*/documents/**")
                        .authenticated()

                        // ================= USER =================

                        .requestMatchers("/api/subjects/**")
                        .hasRole("USER")

                        .requestMatchers("/api/roadmaps/**")
                        .hasRole("USER")

                        .requestMatchers("/api/roadmap-tasks/**")
                        .hasRole("USER")

                        // ================= OTHERS =================

                        .anyRequest().authenticated()

                )

                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of("http://localhost:5173")
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "PATCH",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(List.of("*"));

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();

    }

}