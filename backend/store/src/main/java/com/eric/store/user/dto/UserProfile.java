package com.eric.store.user.dto;

public record UserProfile(
        String name,
        String email,
        boolean emailVerified,
        String provider
) {}
