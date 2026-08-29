package com.muni.backend.security.dto;

import lombok.Data;

@Data
public class ResetPasswordRequest {
    private Integer userId;
    private String newPassword;
}