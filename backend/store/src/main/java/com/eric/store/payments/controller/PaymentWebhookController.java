package com.eric.store.payments.controller;

import com.eric.store.payments.config.StripeConfig;
import com.eric.store.payments.service.PaymentService;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentWebhookController {
    private final PaymentService paymentService;
    private final StripeConfig stripeConfig;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, stripeConfig.getWebhookSecret());
        } catch (SignatureVerificationException e) {
            log.warn("Webhook signature verification failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }

        switch (event.getType()) {
            case "payment_intent.succeeded" -> {
                PaymentIntent intent = (PaymentIntent) deserializePaymentIntent(event);
                if (intent != null) {
                    log.info("Webhook: payment_intent.succeeded for {}", intent.getId());
                    paymentService.handlePaymentIntentSucceeded(intent);
                }
            }
            case "payment_intent.payment_failed" -> {
                PaymentIntent intent = (PaymentIntent) deserializePaymentIntent(event);
                if (intent != null) {
                    log.info("Webhook: payment_intent.payment_failed for {}", intent.getId());
                    paymentService.handlePaymentIntentFailed(intent);
                }
            }
            default -> log.debug("Unhandled webhook event type: {}", event.getType());
        }

        return ResponseEntity.ok("ok");
    }

    private StripeObject deserializePaymentIntent(Event event) {
        // Try typed deserialization first (works when API versions match)
        var deserializer = event.getDataObjectDeserializer();
        if (deserializer.getObject().isPresent()) {
            return deserializer.getObject().get();
        }
        // Fallback: force-deserialize when API version differs
        try {
            return deserializer.deserializeUnsafe();
        } catch (EventDataObjectDeserializationException e) {
            log.error("Could not deserialize webhook event {}: {}", event.getId(), e.getMessage());
            return null;
        }
    }
}
