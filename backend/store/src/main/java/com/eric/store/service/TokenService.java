package com.eric.store.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final JwtService jwtService;
    private final RefreshTokenStore refreshTokenStore;

    public record Pair(String access, String refresh) {}

    public Pair issueTokens(UUID userId) {
        String access = jwtService.generateAccessToken(userId.toString(), Map.of(), 15);
        String refresh = UUID.randomUUID().toString();
        refreshTokenStore.save(refresh, userId, Duration.ofDays(30));
        return new Pair(access, refresh);
    }

    public Pair rotate(String oldRefresh) {
        var userId = refreshTokenStore.getUserId(oldRefresh).orElseThrow();
        refreshTokenStore.delete(oldRefresh);
        String newRefresh = UUID.randomUUID().toString();
        refreshTokenStore.save(newRefresh, userId, Duration.ofDays(30));
        String access = jwtService.generateAccessToken(userId.toString(), Map.of(), 15);
        return new Pair(access, newRefresh);
    }

    public void deleteRefresh(String refresh) {
        refreshTokenStore.delete(refresh);
    }
}
