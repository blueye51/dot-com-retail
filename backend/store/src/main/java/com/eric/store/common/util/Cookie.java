package com.eric.store.common.util;

import org.springframework.http.ResponseCookie;

import java.time.Duration;

public class Cookie {
    public static final String REFRESH = "refreshToken";

    public static ResponseCookie makeRefresh(String value, Duration ttl) {
        return ResponseCookie.from(REFRESH, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("None") // for development with localhost; change to "Strict"  in production
//                .sameSite("Strict")
                .path("/api/auth/refresh")
                .maxAge(ttl)
                .build();
    }

    public static ResponseCookie clearRefresh() {
        return ResponseCookie.from(REFRESH, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None") // for development with localhost; change to "Strict"  in production
//                .sameSite("Strict")
                .path("/api/auth/refresh")
                .maxAge(0)
                .build();
    }
}
