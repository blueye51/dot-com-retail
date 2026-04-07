package com.eric.store.cart.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CartItemResponse(
        UUID id,
        UUID productId,
        String productName,
        BigDecimal price,
        String currency,
        int quantity,
        Integer stock,
        String imageUrl
) {
}
