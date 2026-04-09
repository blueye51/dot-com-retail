package com.eric.store.ratings.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RatingDto(
        UUID id,
        UUID userId,
        String userName,
        UUID productId,
        String productName,
        int score,
        String comment,
        int helpfulCount,
        boolean votedByCurrentUser,
        boolean hidden,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
