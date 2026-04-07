package com.eric.store.common.messaging;

import java.util.UUID;

public record PaymentStatusEvent(
        UUID orderId,
        PaymentStatus status,
        String paymentIntentId,
        String failureReason
) {
    public enum PaymentStatus {
        SUCCESS,
        FAILED
    }
}
