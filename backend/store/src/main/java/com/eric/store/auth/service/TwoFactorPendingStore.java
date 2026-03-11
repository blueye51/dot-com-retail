package com.eric.store.auth.service;

import com.eric.store.common.exceptions.InvalidRefreshTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TwoFactorPendingStore {
    private static final Duration TTL = Duration.ofMinutes(5);
    private final RedisTemplate<String,String> redis;

    private String key(String code) {
        return "2fa-pending:" + code;
    }

    public String save(UUID userId) {
        String code = UUID.randomUUID().toString();
        redis.opsForValue().set(key(code), userId.toString(), TTL);
        return code;
    }

    public UUID consume(String code) {
        String userId = redis.opsForValue().getAndDelete(key(code));
        if (userId == null) throw new InvalidRefreshTokenException("2FA code expired or invalid");
        return UUID.fromString(userId);
    }
}
