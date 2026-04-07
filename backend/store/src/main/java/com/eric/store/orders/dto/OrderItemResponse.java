package com.eric.store.orders.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
        UUID id,
        UUID productId,
        String productName,
        BigDecimal price,
        int quantity,
        BigDecimal totalPrice
) {
}
