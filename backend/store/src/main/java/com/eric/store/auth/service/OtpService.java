package com.eric.store.auth.service;

import com.eric.store.common.exceptions.InvalidOtpException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class OtpService {
    private final EmailService emailService;
    private final RedisTemplate<String, String> redis;
    private final SecureRandom random = new SecureRandom();

    private static final Duration CODE_TTL = Duration.ofMinutes(5);
    private static final Duration SEND_COOLDOWN = Duration.ofSeconds(30);

    public void sendCode(String email, String keyPrefix) {
        String cooldownKey = keyPrefix + "cooldown:" + email;
        if (Boolean.TRUE.equals(redis.hasKey(cooldownKey))) return;
        redis.opsForValue().set(cooldownKey, "1", SEND_COOLDOWN);

        String code = String.format("%06d", random.nextInt(1_000_000));
        redis.opsForValue().set(keyPrefix + email, code, CODE_TTL);
        emailService.sendCodeEmail(email, code);
    }

    public void verifyCode(String email, String code, String keyPrefix) {
        String stored = redis.opsForValue().get(keyPrefix + email);
        if (stored != null && stored.equals(code)) {
            redis.delete(keyPrefix + email);
            return;
        }
        throw new InvalidOtpException("Invalid or expired code");
    }
}
