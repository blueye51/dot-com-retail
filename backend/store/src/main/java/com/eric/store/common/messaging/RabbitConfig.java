package com.eric.store.common.messaging;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String PAYMENT_EXCHANGE = "payment.exchange";

    public static final String PAYMENT_STATUS_QUEUE = "payment.status.queue";
    public static final String PAYMENT_STATUS_ROUTING_KEY = "payment.status";

    public static final String PAYMENT_STATUS_DLQ = "payment.status.dlq";
    public static final String PAYMENT_DLX = "payment.dlx";

    @Bean
    public DirectExchange paymentExchange() {
        return new DirectExchange(PAYMENT_EXCHANGE);
    }

    @Bean
    public DirectExchange paymentDeadLetterExchange() {
        return new DirectExchange(PAYMENT_DLX);
    }

    @Bean
    public Queue paymentStatusQueue() {
        return QueueBuilder.durable(PAYMENT_STATUS_QUEUE)
                .withArgument("x-dead-letter-exchange", PAYMENT_DLX)
                .withArgument("x-dead-letter-routing-key", PAYMENT_STATUS_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue paymentStatusDeadLetterQueue() {
        return QueueBuilder.durable(PAYMENT_STATUS_DLQ).build();
    }

    @Bean
    public Binding paymentStatusBinding(Queue paymentStatusQueue, DirectExchange paymentExchange) {
        return BindingBuilder.bind(paymentStatusQueue).to(paymentExchange).with(PAYMENT_STATUS_ROUTING_KEY);
    }

    @Bean
    public Binding paymentStatusDlqBinding(Queue paymentStatusDeadLetterQueue, DirectExchange paymentDeadLetterExchange) {
        return BindingBuilder.bind(paymentStatusDeadLetterQueue).to(paymentDeadLetterExchange).with(PAYMENT_STATUS_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
