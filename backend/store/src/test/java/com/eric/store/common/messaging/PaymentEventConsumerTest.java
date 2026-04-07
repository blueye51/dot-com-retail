package com.eric.store.common.messaging;

import com.eric.store.auth.service.EmailService;
import com.eric.store.orders.entity.Order;
import com.eric.store.orders.entity.OrderStatus;
import com.eric.store.orders.service.OrderService;
import com.eric.store.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentEventConsumerTest {

    @Mock OrderService orderService;
    @Mock EmailService emailService;

    @InjectMocks PaymentEventConsumer consumer;

    private Order makeOrder(UUID orderId, String email) {
        User user = new User();
        user.setEmail(email);
        Order order = new Order();
        order.setId(orderId);
        order.setUser(user);
        order.setTotalPrice(new BigDecimal("45.50"));
        order.setCurrency("EUR");
        return order;
    }

    @Test
    void handlePaymentStatus_success_updatesOrderAndSendsEmail() {
        UUID orderId = UUID.randomUUID();
        PaymentStatusEvent event = new PaymentStatusEvent(
                orderId, PaymentStatusEvent.PaymentStatus.SUCCESS, "pi_123", null);

        when(orderService.findByIdWithUser(orderId)).thenReturn(makeOrder(orderId, "buyer@test.com"));

        consumer.handlePaymentStatus(event);

        verify(orderService).handlePaymentSuccess(orderId, "pi_123");
        verify(emailService).sendEmail(eq("buyer@test.com"), contains("Order Confirmed"), anyString());
    }

    @Test
    void handlePaymentStatus_failure_updatesOrderAndSendsEmail() {
        UUID orderId = UUID.randomUUID();
        PaymentStatusEvent event = new PaymentStatusEvent(
                orderId, PaymentStatusEvent.PaymentStatus.FAILED, "pi_456", "Card declined");

        when(orderService.findByIdWithUser(orderId)).thenReturn(makeOrder(orderId, "buyer@test.com"));

        consumer.handlePaymentStatus(event);

        verify(orderService).handlePaymentFailure(orderId, "pi_456", "Card declined");
        verify(emailService).sendEmail(eq("buyer@test.com"), contains("Payment Failed"), anyString());
    }

    @Test
    void handlePaymentStatus_emailFailure_doesNotThrow() {
        UUID orderId = UUID.randomUUID();
        PaymentStatusEvent event = new PaymentStatusEvent(
                orderId, PaymentStatusEvent.PaymentStatus.SUCCESS, "pi_789", null);

        when(orderService.findByIdWithUser(orderId)).thenReturn(makeOrder(orderId, "buyer@test.com"));
        doThrow(new RuntimeException("Email service down")).when(emailService)
                .sendEmail(anyString(), anyString(), anyString());

        // Should not throw — email failure is logged but doesn't break the flow
        consumer.handlePaymentStatus(event);

        verify(orderService).handlePaymentSuccess(orderId, "pi_789");
    }
}
