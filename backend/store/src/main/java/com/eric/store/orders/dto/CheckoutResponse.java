package com.eric.store.orders.dto;

import java.util.UUID;

public record CheckoutResponse(
        UUID orderId,
        String clientSecret
) {
}
