package com.eric.store.auth.service;


import com.eric.store.user.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Service
public class TokenService {
    private final JwtService jwtService;
    private final RefreshTokenStore refreshTokenStore;
    private final UserService userService;
    private final Duration refreshExpiration;

    public TokenService(JwtService jwtService,
                        RefreshTokenStore refreshTokenStore,
                        UserService userService,
                        @Value("${app.jwt.refresh-expiration-days}") long refreshDays) {
        this.jwtService = jwtService;
        this.refreshTokenStore = refreshTokenStore;
        this.userService = userService;
        this.refreshExpiration = Duration.ofDays(refreshDays);
    }

    public record Pair(String access, String refresh) {}

    public Pair issueTokens(UUID userId) {
        return buildPair(userId);
    }

    public Pair rotate(String oldRefresh) {
        UUID userId = refreshTokenStore.getUserId(oldRefresh);
        refreshTokenStore.delete(oldRefresh);
        return buildPair(userId);
    }

    public void deleteRefresh(String refresh) {
        refreshTokenStore.delete(refresh);
    }

    public UUID getUserId(String refresh) {
        return refreshTokenStore.getUserId(refresh);
    }

    private Pair buildPair(UUID userId) {
        Map<String, Object> claims = Map.of("roles", userService.getAllRolesFromId(userId));
        String access = jwtService.generateAccessToken(userId.toString(), claims);
        String refresh = UUID.randomUUID().toString();
        refreshTokenStore.save(refresh, userId, refreshExpiration);
        return new Pair(access, refresh);
    }
}
