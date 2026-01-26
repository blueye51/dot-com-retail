package com.eric.store.products.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        String description,
        String price,
        String currency,
        String width,
        String height,
        String depth,
        String weight,
        Integer stock,
        UUID categoryId,
        OffsetDateTime createdAt,
        List<ImageCreate> images
) {
}
