package com.eric.store.user.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record AdminUserDto(
        UUID id,
        String name,
        String email,
        String provider,
        boolean emailVerified,
        List<String> roles,
        OffsetDateTime createdAt
) {
}
