package com.aimentor.ai_mentor_be.repository;

import com.aimentor.ai_mentor_be.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByResetToken(String token);

    // API 1
    long countByLastLoginIsNotNull();

    // API 3
    List<User> findByCreatedAtAfter(Timestamp time);
    List<User> findAllByOrderByCreatedAtDesc();
    long countByCreatedAtAfter(Timestamp time);

    List<User> findByRoleRoleName(String roleName);
}