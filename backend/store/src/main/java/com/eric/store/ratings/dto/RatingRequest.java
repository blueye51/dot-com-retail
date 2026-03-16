package com.eric.store.ratings.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RatingRequest(
        @NotNull UUID productId,
        @Min(1) @Max(5) int score,
        String comment
        ) {
}
