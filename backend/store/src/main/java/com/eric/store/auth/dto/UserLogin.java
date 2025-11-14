package com.eric.store.auth.dto;

public record UserLogin(
    String email,
    String password
) {}
