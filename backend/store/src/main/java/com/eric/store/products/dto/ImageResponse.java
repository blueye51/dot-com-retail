package com.eric.store.products.dto;

public record ImageResponse(
        String key,
        String url,
        Integer sortOrder
) {
}
