package com.eric.store.dto;

import jakarta.validation.constraints.Email;

public record UserRegister(
        @Email String email,
        String password,
        String name
) {}
