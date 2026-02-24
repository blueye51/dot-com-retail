package com.eric.store.auth.security;

import com.eric.store.common.exceptions.InvalidRefreshTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

import static com.eric.store.common.util.HashUtils.sha256;

@Service
@RequiredArgsConstructor
public class OAuth2LoginCodeStore {

    private final RedisTemplate<String, String> redis;

    private String key(String hash) {
        return "oauth2LoginCode:" + hash;
    }

    public void save(String rawCode, UUID userId, Duration ttl) {
        redis.opsForValue().set(key(sha256(rawCode)), userId.toString(), ttl);
    }

    public UUID consume(String rawCode) {
        String k = key(sha256(rawCode));
        String userId = redis.opsForValue().get(k);
        if (userId == null) {
            throw new InvalidRefreshTokenException("Login code not found/expired");
        }
        redis.delete(k);
        return UUID.fromString(userId);
    }
}