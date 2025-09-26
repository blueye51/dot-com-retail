package com.eric.store.controller;

import com.eric.store.dto.UserLogin;
import com.eric.store.dto.UserRegister;
import com.eric.store.entity.User;
import com.eric.store.service.AuthService;
import com.eric.store.service.TokenService;
import com.eric.store.service.UserService;
import com.eric.store.util.Cookie;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final TokenService tokenService;
    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody UserRegister userRegister) {
        authService.register(userRegister);
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String,String>> login(@Valid @RequestBody UserLogin userLogin) {
        User user = authService.login(userLogin);
        var pair = tokenService.issueTokens(user.getId());
        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, Cookie.makeRefresh(pair.refresh(), Duration.ofDays(30)).toString())
                .body(Map.of("accessToken", pair.access()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String,String>> refresh(@CookieValue(name = Cookie.REFRESH, required = false ) String refreshCookie) {
        if (refreshCookie == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try {
            var pair = tokenService.rotate(refreshCookie);
            return ResponseEntity.status(HttpStatus.OK)
                    .header(HttpHeaders.SET_COOKIE, Cookie.makeRefresh(pair.refresh(), Duration.ofDays(30)).toString())
                    .body(Map.of("accessToken", pair.access()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @DeleteMapping("/refresh/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = Cookie.REFRESH, required = false ) String refreshCookie) {
            if (refreshCookie != null) tokenService.deleteRefresh(refreshCookie);
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .header(HttpHeaders.SET_COOKIE, Cookie.clearRefresh().toString())
                    .build();
        }
}
