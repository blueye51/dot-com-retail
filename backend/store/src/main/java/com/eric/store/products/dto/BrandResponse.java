package com.eric.store.products.dto;

import java.util.UUID;

public record BrandResponse(
        UUID id,
        String name
) {
}
