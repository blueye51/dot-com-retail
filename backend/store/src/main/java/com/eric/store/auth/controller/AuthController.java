package com.eric.store.auth.controller;

import com.eric.store.auth.service.*;
import com.eric.store.user.dto.UserLogin;
import com.eric.store.user.dto.UserRegister;
import com.eric.store.common.exceptions.InvalidRefreshTokenException;
import com.eric.store.user.entity.User;
import com.eric.store.user.service.UserService;
import com.eric.store.common.util.Cookie;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final TokenService tokenService;
    private final UserService userService;
    private final OAuth2LoginCodeStore loginCodeStore;
    private final TurnstileService turnstileService;
    private final TwoFactorAuthService twoFactorAuthService;
    private final TwoFactorPendingStore twoFactorPendingStore;
    private final Cookie cookie;

    public record Verify2faRequest(String tempCode, String otpCode) {}

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody UserRegister userRegister) {
        turnstileService.verifyOrThrow(userRegister.turnstileToken());
        userService.register(userRegister);
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody UserLogin userLogin) {
        turnstileService.verifyOrThrow(userLogin.turnstileToken());
        User user = userService.login(userLogin);
        if (user.getSettings().isTwoFactorEnabled()) {
            twoFactorAuthService.sendCode(user.getEmail());
            String tempCode = twoFactorPendingStore.save(user.getId());
            return ResponseEntity.ok(Map.of(
                    "2faRequired", true,
                    "tempCode", tempCode
            ));
        }

        var pair = tokenService.issueTokens(user.getId());
        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, cookie.makeRefresh(pair.refresh()).toString())
                .body(Map.of(
                        "accessToken", pair.access()
                ));
    }

    @PostMapping("/login/verify-2fa")
    public ResponseEntity<Map<String, Object>> verify2fa(@Valid @RequestBody Verify2faRequest req) {
        UUID userId = twoFactorPendingStore.consume(req.tempCode());
        User user = userService.findById(userId);
        twoFactorAuthService.verifyCode(user.getEmail(), req.otpCode());

        var pair = tokenService.issueTokens(userId);
        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, cookie.makeRefresh(pair.refresh()).toString())
                .body(Map.of(
                        "accessToken", pair.access()
                ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refresh(@CookieValue(name = Cookie.REFRESH, required = false) String refreshCookie) {
        if (refreshCookie == null) throw new InvalidRefreshTokenException("Missing refresh cookie");
        var user = userService.findById(tokenService.getUserId(refreshCookie));
        var pair = tokenService.rotate(refreshCookie);
        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, cookie.makeRefresh(pair.refresh()).toString())
                .body(Map.of(
                        "accessToken", pair.access()
                ));
    }

    @DeleteMapping("/refresh/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = Cookie.REFRESH, required = false) String refreshCookie) {
        if (refreshCookie != null) tokenService.deleteRefresh(refreshCookie);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .header(HttpHeaders.SET_COOKIE, cookie.clearRefresh().toString())
                .build();
    }

    public record CodeExchangeRequest(String code) {}

    @PostMapping(value = "/oauth2/exchange", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> exchange(@RequestBody CodeExchangeRequest req) {
        var userId = loginCodeStore.consume(req.code());
        var pair = tokenService.issueTokens(userId);

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, cookie.makeRefresh(pair.refresh()).toString())
                .body(Map.of(
                        "accessToken", pair.access()
                ));
    }


}
