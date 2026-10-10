package com.muni.backend.security.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String rol;
    private String username;

    public AuthResponse(String token) {
        this.token = token;
    }
}
