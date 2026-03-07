package com.eric.store.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TwoFactorAuthService {
    private final EmailService emailService;
    private final RedisTemplate<String, String> redis;
    private final SecureRandom random = new SecureRandom();

    private static final Duration CODE_TTL = Duration.ofMinutes(5);
    private static final String KEY_PREFIX = "2fa:";

    public void sendCode(String email) {
        String code = String.format("%06d", random.nextInt(1_000_000));
        redis.opsForValue().set(KEY_PREFIX + email, code, CODE_TTL);
        emailService.sendCodeEmail(email, code);
    }

    public boolean verifyCode(String email, String code) {
        String stored = redis.opsForValue().get(KEY_PREFIX + email);
        if (stored != null && stored.equals(code)) {
            redis.delete(KEY_PREFIX + email);
            return true;
        }
        return false;
    }
}
