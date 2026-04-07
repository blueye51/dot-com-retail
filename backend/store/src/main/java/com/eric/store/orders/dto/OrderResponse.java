package com.eric.store.orders.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String status,
        BigDecimal totalPrice,
        String currency,
        String paymentIntentId,
        String failureReason,
        List<OrderItemResponse> items,
        AddressResponse shippingAddress,
        String shippingMethod,
        BigDecimal shippingCost,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
