package com.aimentor.ai_mentor_be.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentCountResponse {

    private Long subjectId;
    private String subjectName;
    private long totalDocuments;
    private long totalPdf;
    private long totalDocx;
}