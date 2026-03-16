package com.eric.store.products.dto;

import com.eric.store.products.entity.CurrencyProvider;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ProductCard(
        UUID id,
        String name,
        BigDecimal price,
        CurrencyProvider currency,
        String brand,
        Integer stock,
        double averageRating,
        int totalRatings,
        String category,
        String imageUrl
) {
}
