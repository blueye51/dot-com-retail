package com.eric.store.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenStore {
    private final RedisTemplate<String,String> redis;

    private String key(String hash){
        return "refresh_token:" + hash;
    }

    public void save(String token, UUID userId, Duration ttl) {
        redis.opsForValue().set(key(token), userId.toString(), ttl);
    }

    public UUID validate(String token) {
        String userId = redis.opsForValue().get(key(token));
        return userId == null ? null : UUID.fromString(userId);
    }

    public void revoke(String token) {
        redis.delete(key(token));
    }
}
