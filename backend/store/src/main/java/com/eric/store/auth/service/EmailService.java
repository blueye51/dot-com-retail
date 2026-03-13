package com.eric.store.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class EmailService {
    private final RestClient restClient;
    private final String from;

    public EmailService(RestClient.Builder builder,
                        @Value("${app.resend.from-email}") String fromEmail,
                        @Value("${app.resend.api-key}") String apiKey) {
        this.restClient = builder
                .baseUrl("https://api.resend.com")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
        this.from = fromEmail;
    }

    public record EmailRequest(String from, String to, String subject, String html) {}

    @Async
    public void sendEmail(String to, String subject, String html) {
        restClient.post()
                .uri("/emails")
                .body(new EmailRequest(from, to, subject, html))
                .retrieve()
                .toBodilessEntity();
    }

    public void sendCodeEmail(String to, String code) {
        String html = """
                <p>Your verification code is: <strong>%s</strong></p>
                <p>This code expires in 5 minutes and can only be used once.</p>
                <p>If you didn't request this, you can safely ignore this email.</p>
                """.formatted(code);
        String subject = code + " is your Store verification code";
        sendEmail(to, subject, html);
    }

    public void sendPasswordResetEmail(String to, String link) {
        String html = """
                <p>Click the link below to reset your password:</p>
                <p><a href="%s">Reset Password</a></p>
                <p>This link expires in 15 minutes and can only be used once.</p>
                <p>If you didn't request this, you can safely ignore this email.</p>
                """.formatted(link);
        sendEmail(to, "Reset your password", html);
    }

    public void sendVerificationLinkEmail(String to, String link) {
        String html = """
                <p>Click the link below to verify your email address:</p>
                <p><a href="%s">Verify Email</a></p>
                <p>This link expires in 15 minutes and can only be used once.</p>
                <p>If you didn't request this, you can safely ignore this email.</p>
                """.formatted(link);
        sendEmail(to, "Verify your email address", html);
    }
}
