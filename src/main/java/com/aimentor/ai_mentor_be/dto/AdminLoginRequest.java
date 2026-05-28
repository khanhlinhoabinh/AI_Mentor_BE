package com.aimentor.ai_mentor_be.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminLoginRequest {

    private String email;

    private String password;
}