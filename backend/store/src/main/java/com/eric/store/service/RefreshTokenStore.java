package com.eric.store.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static com.eric.store.util.HashUtils.sha256;

@Service
@RequiredArgsConstructor
public class RefreshTokenStore {
    private final RedisTemplate<String,String> redis;

    private String key(String hash){
        return "refreshToken:" + hash;
    }

    public void save(String raw, UUID userId, Duration ttl) {
        redis.opsForValue().set(key(sha256(raw)), userId.toString(), ttl);
    }

    public Optional<UUID> getUserId(String raw) {
        String userId = redis.opsForValue().get(key(sha256(raw)));
        return userId == null ? Optional.empty() : Optional.of(UUID.fromString(userId));
    }

    public void delete(String raw) {
        redis.delete(key(sha256(raw)));
    }
}
