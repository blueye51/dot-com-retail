package com.eric.store.common.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class Cookie {
    public static final String REFRESH = "refreshToken";

    private final Duration refreshTtl;
    private final String sameSite;

    public Cookie(@Value("${app.jwt.refresh-expiration-days}") long refreshDays,
                  @Value("${app.cookie.same-site}") String sameSite) {
        this.refreshTtl = Duration.ofDays(refreshDays);
        this.sameSite = sameSite;
    }

    public ResponseCookie makeRefresh(String value) {
        return buildBase(value)
                .maxAge(refreshTtl)
                .build();
    }

    public ResponseCookie clearRefresh() {
        return buildBase("")
                .maxAge(0)
                .build();
    }

    private ResponseCookie.ResponseCookieBuilder buildBase(String value) {
        return ResponseCookie.from(REFRESH, value)
                .httpOnly(true)
                .secure(true)
                .sameSite(sameSite)
                .path("/api/auth/refresh");
    }
}
