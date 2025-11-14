package com.eric.store.auth.security;


import com.eric.store.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final JwtService jwtService;
    private final RefreshTokenStore refreshTokenStore;
    private final UserService userService;

    public record Pair(String access, String refresh) {}

    public Pair issueTokens(UUID userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userService.getAllRolesFromId(userId));
        String access = jwtService.generateAccessToken(userId.toString(), claims, 15);
        String refresh = UUID.randomUUID().toString();
        refreshTokenStore.save(refresh, userId, Duration.ofDays(30));
        return new Pair(access, refresh);
    }

    public Pair rotate(String oldRefresh) {
        UUID userId = refreshTokenStore.getUserId(oldRefresh);
        refreshTokenStore.delete(oldRefresh);
        String newRefresh = UUID.randomUUID().toString();
        refreshTokenStore.save(newRefresh, userId, Duration.ofDays(30));
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userService.getAllRolesFromId(userId));
        String access = jwtService.generateAccessToken(userId.toString(), claims, 15);
        return new Pair(access, newRefresh);
    }

    public void deleteRefresh(String refresh) {
        refreshTokenStore.delete(refresh);
    }

    public UUID getUserId(String refresh) {
        return refreshTokenStore.getUserId(refresh);
    }
}
