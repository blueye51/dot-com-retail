package com.eric.store.auth.controller;

import com.eric.store.auth.dto.UserLogin;
import com.eric.store.auth.dto.UserRegister;
import com.eric.store.auth.entity.Role;
import com.eric.store.common.exceptions.InvalidRefreshTokenException;
import com.eric.store.user.entity.User;
import com.eric.store.auth.service.TokenService;
import com.eric.store.user.service.UserService;
import com.eric.store.common.util.Cookie;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.eric.store.auth.security.OAuth2LoginCodeStore;
import org.springframework.http.MediaType;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final TokenService tokenService;
    private final UserService userService;
    private final OAuth2LoginCodeStore loginCodeStore;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody UserRegister userRegister) {
        userService.register(userRegister);
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody UserLogin userLogin) {
        User user = userService.login(userLogin);
        var pair = tokenService.issueTokens(user.getId());
        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, Cookie.makeRefresh(pair.refresh(), Duration.ofDays(30)).toString())
                .body(Map.of(
                        "accessToken", pair.access(),
                        "roles", user.getRoles().stream()
                                .map(Role::getName)
                                .toList()
                ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refresh(@CookieValue(name = Cookie.REFRESH, required = false) String refreshCookie) {
        if (refreshCookie == null) throw new InvalidRefreshTokenException("Missing refresh cookie");
        var user = userService.findById(tokenService.getUserId(refreshCookie));
        var pair = tokenService.rotate(refreshCookie);
        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, Cookie.makeRefresh(pair.refresh(), Duration.ofDays(30)).toString())
                .body(Map.of(
                        "accessToken", pair.access(),
                        "roles", user.getRoles().stream()
                                .map(Role::getName)
                                .toList()
                ));


    }

    @DeleteMapping("/refresh/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = Cookie.REFRESH, required = false) String refreshCookie) {
        if (refreshCookie != null) tokenService.deleteRefresh(refreshCookie);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .header(HttpHeaders.SET_COOKIE, Cookie.clearRefresh().toString())
                .build();
    }

    public record CodeExchangeRequest(String code) {}

    @PostMapping(value = "/oauth2/exchange", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> exchange(@RequestBody CodeExchangeRequest req) {
        var userId = loginCodeStore.consume(req.code());
        var user = userService.findById(userId);
        var pair = tokenService.issueTokens(userId);

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, Cookie.makeRefresh(pair.refresh(), Duration.ofDays(30)).toString())
                .body(Map.of(
                        "accessToken", pair.access(),
                        "roles", user.getRoles().stream().map(Role::getName).toList()
                ));
    }
}
