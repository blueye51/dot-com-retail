package com.eric.store.products.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        String description,
        BigDecimal price,
        String currency,
        BigDecimal width,
        BigDecimal height,
        BigDecimal depth,
        BigDecimal weight,
        Integer stock,
        UUID categoryId,
        OffsetDateTime createdAt,
        List<ImageResponse> images
) {
}
