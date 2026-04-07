package com.eric.store.payments.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class StripeConfig {

    @Value("${app.stripe.secret-key}")
    private String secretKey;

    @Value("${app.stripe.webhook-secret}")
    private String webhookSecret;

    @PostConstruct
    void init() {
        Stripe.apiKey = secretKey;
    }
}
