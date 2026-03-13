package com.eric.store.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {
    private static final String PREFIX = "password-reset:";
    private static final String COOLDOWN_PREFIX = PREFIX + "cooldown:";
    private static final Duration TOKEN_TTL = Duration.ofMinutes(15);
    private static final Duration SEND_COOLDOWN = Duration.ofSeconds(30);

    private final RedisTemplate<String, String> redis;
    private final EmailService emailService;

    public void sendResetLink(String email, String frontendBaseUrl) {
        String cooldownKey = COOLDOWN_PREFIX + email;
        if (Boolean.TRUE.equals(redis.hasKey(cooldownKey))) return;
        redis.opsForValue().set(cooldownKey, "1", SEND_COOLDOWN);

        String token = UUID.randomUUID().toString();
        redis.opsForValue().set(PREFIX + token, email, TOKEN_TTL);

        String link = frontendBaseUrl + "/reset-password/" + token;
        emailService.sendPasswordResetEmail(email, link);
    }

    /**
     * Validates the token and returns the associated email.
     */
    public String verifyToken(String token) {
        String key = PREFIX + token;
        String email = redis.opsForValue().get(key);
        if (email == null) {
            throw new IllegalArgumentException("Invalid or expired reset link");
        }
        redis.delete(key);
        return email;
    }
}
