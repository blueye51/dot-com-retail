package com.eric.store.brands.dto;

import java.util.UUID;

public record BrandResponse(
        UUID id,
        String name
) {
}
