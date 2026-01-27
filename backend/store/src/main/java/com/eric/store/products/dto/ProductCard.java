package com.eric.store.products.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ProductCard(
        UUID id,
        String name,
        BigDecimal price,
        String currency,
        Integer stock,
        UUID categoryId,
        OffsetDateTime createdAt,
        String imageUrl
) {
}
