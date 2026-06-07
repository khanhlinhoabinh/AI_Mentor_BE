package com.aimentor.ai_mentor_be.service;

import com.aimentor.ai_mentor_be.dto.CreateSubjectRequest;
import com.aimentor.ai_mentor_be.dto.SubjectResponse;
import com.aimentor.ai_mentor_be.dto.UpdateSubjectRequest;
import com.aimentor.ai_mentor_be.entity.Subject;
import com.aimentor.ai_mentor_be.entity.User;
import com.aimentor.ai_mentor_be.repository.SubjectRepository;
import com.aimentor.ai_mentor_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectRepository subjectRepository;

    private final UserRepository userRepository;

    // CREATE SUBJECT
    public SubjectResponse createSubject(
            UUID userId,
            CreateSubjectRequest request
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Subject subject = Subject.builder()
                .user(user)
                .subjectName(request.getSubjectName())
                .description(request.getDescription())
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .build();

        Subject savedSubject = subjectRepository.save(subject);

        return mapToResponse(savedSubject);
    }

    // GET ALL SUBJECTS
    public List<SubjectResponse> getSubjectsByUser(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return subjectRepository.findByUser(user)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // SEARCH SUBJECT
    public List<SubjectResponse> searchSubjects(
            UUID userId,
            String keyword
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return subjectRepository
                .findByUserAndSubjectNameContainingIgnoreCase(
                        user,
                        keyword
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // GET SUBJECT DETAIL
    public SubjectResponse getSubjectDetail(
            UUID userId,
            Long subjectId
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() ->
                        new RuntimeException("Subject not found"));

        if (!subject.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("You do not have permission");
        }

        return mapToResponse(subject);
    }

    // UPDATE SUBJECT
    public SubjectResponse updateSubject(
            UUID userId,
            Long subjectId,
            UpdateSubjectRequest request
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() ->
                        new RuntimeException("Subject not found"));

        if (!subject.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("You do not have permission");
        }

        subject.setSubjectName(request.getSubjectName());
        subject.setDescription(request.getDescription());
        subject.setUpdatedAt(
                new Timestamp(System.currentTimeMillis())
        );

        Subject updatedSubject = subjectRepository.save(subject);

        return mapToResponse(updatedSubject);
    }

    // DELETE SUBJECT
    public void deleteSubject(
            UUID userId,
            Long subjectId
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() ->
                        new RuntimeException("Subject not found"));

        if (!subject.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("You do not have permission");
        }

        subjectRepository.delete(subject);
    }

    private SubjectResponse mapToResponse(
            Subject subject
    ) {

        return SubjectResponse.builder()
                .subjectId(subject.getSubjectId())
                .subjectName(subject.getSubjectName())
                .description(subject.getDescription())
                .createdAt(subject.getCreatedAt())
                .updatedAt(subject.getUpdatedAt())
                .build();
    }
}