package com.aimentor.ai_mentor_be.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class GenerateQuizRequest {
    private String sourceType;  // TEXT | FILE (dùng khi gọi qua form-data)
    private String sourceText;  // nội dung dán trực tiếp (khi sourceType=TEXT)
}