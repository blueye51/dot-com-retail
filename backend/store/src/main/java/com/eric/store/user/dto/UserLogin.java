package com.eric.store.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UserLogin(
        @NotBlank String email,

        @NotBlank String password,

        @NotBlank String turnstileToken
) {}
