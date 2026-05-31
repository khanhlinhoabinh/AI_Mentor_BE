package com.aimentor.ai_mentor_be.repository;

import com.aimentor.ai_mentor_be.entity.Subject;
import com.aimentor.ai_mentor_be.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    List<Subject> findByUser(User user);
}