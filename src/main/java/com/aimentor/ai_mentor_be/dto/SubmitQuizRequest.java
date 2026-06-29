package com.aimentor.ai_mentor_be.dto;

import lombok.*;
import java.util.Map;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SubmitQuizRequest {
    // key: questionId, value: đáp án user chọn (JSON string)
    private Map<Long, String> answers;
}