package com.eric.store.payments.service;

import com.eric.store.common.messaging.PaymentEventPublisher;
import com.eric.store.common.messaging.PaymentStatusEvent;
import com.eric.store.orders.entity.Order;
import com.eric.store.orders.repository.OrderRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    private final OrderRepository orderRepository;
    private final PaymentEventPublisher paymentEventPublisher;

    /**
     * Creates a Stripe PaymentIntent for the given order.
     * Stores the paymentIntentId on the order for idempotent webhook processing.
     * Returns the client secret for the frontend to confirm payment.
     */
    @Transactional
    public String createPaymentIntent(Order order) {
        // Stripe expects amount in smallest currency unit (cents for EUR/USD)
        long amountInCents = order.getTotalPrice()
                .multiply(BigDecimal.valueOf(100))
                .longValueExact();

        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency(order.getCurrency().toLowerCase())
                    .putMetadata("orderId", order.getId().toString())
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build())
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);

            order.setPaymentIntentId(intent.getId());
            orderRepository.save(order);

            log.info("Created PaymentIntent {} for order {}", intent.getId(), order.getId());
            return intent.getClientSecret();
        } catch (StripeException e) {
            log.error("Stripe error creating PaymentIntent for order {}: {}", order.getId(), e.getMessage());
            throw new RuntimeException("Payment initialization failed", e);
        }
    }

    /**
     * Issues a full refund for a PaymentIntent via Stripe.
     */
    public void refund(String paymentIntentId) {
        try {
            RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(paymentIntentId)
                    .build();
            Refund refund = Refund.create(params);
            log.info("Refund {} created for PaymentIntent {}", refund.getId(), paymentIntentId);
        } catch (StripeException e) {
            log.error("Stripe refund failed for PaymentIntent {}: {}", paymentIntentId, e.getMessage());
            throw new RuntimeException("Refund failed", e);
        }
    }

    /**
     * Called by the webhook controller when Stripe notifies us of payment result.
     * Publishes the result to RabbitMQ for the order service to consume.
     */
    public void handlePaymentIntentSucceeded(PaymentIntent intent) {
        String orderId = intent.getMetadata().get("orderId");
        if (orderId == null) {
            log.warn("PaymentIntent {} has no orderId metadata, skipping", intent.getId());
            return;
        }

        paymentEventPublisher.publishPaymentStatus(new PaymentStatusEvent(
                java.util.UUID.fromString(orderId),
                PaymentStatusEvent.PaymentStatus.SUCCESS,
                intent.getId(),
                null
        ));
    }

    public void handlePaymentIntentFailed(PaymentIntent intent) {
        String orderId = intent.getMetadata().get("orderId");
        if (orderId == null) {
            log.warn("PaymentIntent {} has no orderId metadata, skipping", intent.getId());
            return;
        }

        String reason = null;
        if (intent.getLastPaymentError() != null) {
            reason = intent.getLastPaymentError().getMessage();
        }

        paymentEventPublisher.publishPaymentStatus(new PaymentStatusEvent(
                java.util.UUID.fromString(orderId),
                PaymentStatusEvent.PaymentStatus.FAILED,
                intent.getId(),
                reason
        ));
    }
}
