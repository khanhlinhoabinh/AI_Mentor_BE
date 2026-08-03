package com.aimentor.ai_mentor_be.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModerationResult {

    private Boolean safe;
    private String riskLevel;      // NONE | LOW | MEDIUM | HIGH
    private Boolean hasViolation;
    private List<String> categories;
    private String summary;
    private String warning;
}