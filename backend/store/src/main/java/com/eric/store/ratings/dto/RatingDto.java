package com.eric.store.ratings.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RatingDto(
        UUID id,
        UUID userId,
        UUID productId,
        int score,
        String comment,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
