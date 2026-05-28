package com.aimentor.ai_mentor_be.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {

    private String token;

    private String newPassword;
}