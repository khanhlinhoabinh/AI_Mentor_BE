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
                .csrf(csrf -> csrf.disable()) // Vô hiệu hóa CSRF cho API
                .authorizeHttpRequests(auth -> auth
                        // 1. Công khai các API xác thực
                        .requestMatchers("/api/auth/**").permitAll()

                        // 2. CHO PHÉP TẤT CẢ OPTIONS (Sửa lỗi 403 khi gọi POST/PUT/DELETE từ Postman/Frontend)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 3. Các API cần xác thực
                        .requestMatchers("/api/quiz/**").authenticated()
                        .requestMatchers("/api/subjects/*/documents/*/file").authenticated()
                        .requestMatchers("/api/chat/**").authenticated()
                        .requestMatchers("/api/subjects/*/documents/**").authenticated()

                        // 4. Các API cần quyền ROLE_USER
                        .requestMatchers("/api/roadmap-tasks/**").hasRole("USER")
                        .requestMatchers("/api/subjects/**").hasRole("USER")
                        .requestMatchers("/api/roadmaps/**").hasRole("USER")
                        .requestMatchers("/api/reminders/**").authenticated()
                        .requestMatchers("/api/notifications/**").authenticated()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}