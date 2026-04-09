package com.eric.store.orders.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminOrderSummary(
        UUID id,
        String status,
        BigDecimal totalPrice,
        String currency,
        int itemCount,
        String userEmail,
        String userName,
        OffsetDateTime createdAt
) {
}
