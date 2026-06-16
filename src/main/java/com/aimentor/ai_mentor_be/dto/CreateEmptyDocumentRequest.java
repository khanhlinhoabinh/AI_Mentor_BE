package com.aimentor.ai_mentor_be.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateEmptyDocumentRequest {
    private String fileName; // tên file người dùng đặt
}