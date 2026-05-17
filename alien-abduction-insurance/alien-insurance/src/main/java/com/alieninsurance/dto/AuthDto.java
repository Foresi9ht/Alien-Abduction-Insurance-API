package com.alieninsurance.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class AuthDto {

    @Data
    public static class RegisterRequest {
        @NotBlank private String username;
        @NotBlank @Size(min = 6) private String password;
        @NotBlank @Email private String email;
    }

    @Data
    public static class LoginRequest {
        @NotBlank private String username;
        @NotBlank private String password;
    }

    @Data
    public static class AuthResponse {
        private final String token;
        private final String username;
        private final String role;
    }
}
