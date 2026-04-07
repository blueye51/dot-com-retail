package com.eric.store.common.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    public void publishPaymentStatus(PaymentStatusEvent event) {
        log.info("Publishing payment status event: orderId={}, status={}", event.orderId(), event.status());
        rabbitTemplate.convertAndSend(
                RabbitConfig.PAYMENT_EXCHANGE,
                RabbitConfig.PAYMENT_STATUS_ROUTING_KEY,
                event
        );
    }
}
