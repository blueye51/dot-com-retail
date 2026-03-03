package com.eric.store.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserRegister(
        @NotBlank
        String email,

        @NotBlank
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).{8,}$",
                message = "Password must be at least 8 characters long and include uppercase, lowercase, number, and symbol"
        )
        String password,

        @NotBlank String name,

        @NotBlank String turnstileToken
) {}
