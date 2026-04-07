package com.eric.store.common.messaging;

import com.eric.store.auth.service.EmailService;
import com.eric.store.orders.entity.Order;
import com.eric.store.orders.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {
    private final OrderService orderService;
    private final EmailService emailService;

    @RabbitListener(queues = RabbitConfig.PAYMENT_STATUS_QUEUE)
    public void handlePaymentStatus(PaymentStatusEvent event) {
        log.info("Received payment status event: orderId={}, status={}", event.orderId(), event.status());

        try {
            switch (event.status()) {
                case SUCCESS -> {
                    orderService.handlePaymentSuccess(event.orderId(), event.paymentIntentId());
                    sendConfirmationEmail(event);
                }
                case FAILED -> {
                    orderService.handlePaymentFailure(event.orderId(), event.paymentIntentId(), event.failureReason());
                    sendFailureEmail(event);
                }
            }
        } catch (Exception e) {
            log.error("Failed to process payment event for order {}: {}", event.orderId(), e.getMessage(), e);
            throw e; // re-throw so RabbitMQ can retry or send to DLQ
        }
    }

    private void sendConfirmationEmail(PaymentStatusEvent event) {
        try {
            Order order = orderService.findByIdWithUser(event.orderId());
            String email = order.getUser().getEmail();
            String html = """
                    <h2>Order Confirmed</h2>
                    <p>Your order <strong>%s</strong> has been successfully paid.</p>
                    <p>Total: <strong>%s %s</strong></p>
                    <p>Thank you for your purchase!</p>
                    """.formatted(order.getId(), order.getTotalPrice(), order.getCurrency());
            emailService.sendEmail(email, "Order Confirmed - " + order.getId(), html);
        } catch (Exception e) {
            log.error("Failed to send confirmation email for order {}: {}", event.orderId(), e.getMessage());
        }
    }

    private void sendFailureEmail(PaymentStatusEvent event) {
        try {
            Order order = orderService.findByIdWithUser(event.orderId());
            String email = order.getUser().getEmail();
            String reason = event.failureReason() != null ? event.failureReason() : "Unknown error";
            String html = """
                    <h2>Payment Failed</h2>
                    <p>We were unable to process payment for your order <strong>%s</strong>.</p>
                    <p>Reason: %s</p>
                    <p>Please try again or use a different payment method.</p>
                    """.formatted(order.getId(), reason);
            emailService.sendEmail(email, "Payment Failed - " + order.getId(), html);
        } catch (Exception e) {
            log.error("Failed to send failure email for order {}: {}", event.orderId(), e.getMessage());
        }
    }
}
