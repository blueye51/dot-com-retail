package com.eric.store.auth.controller;

import com.eric.store.auth.service.EmailVerificationService;
import com.eric.store.user.entity.User;
import com.eric.store.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/email-verification")
@RequiredArgsConstructor
public class EmailVerificationController {
    private final EmailVerificationService verificationService;
    private final UserService userService;

    @Value("${app.frontend-base-url}")
    private String frontendBaseUrl;

    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendVerificationLink(@AuthenticationPrincipal UUID userId) {
        User user = userService.findById(userId);
        verificationService.sendVerificationLink(user.getEmail(), frontendBaseUrl);
        return ResponseEntity.ok().body(Map.of("email", user.getEmail()));
    }

    @GetMapping("/verify")
    public ResponseEntity<Void> verifyEmail(@RequestParam String token) {
        String email = verificationService.verifyToken(token);
        User user = userService.findByEmail(email);
        userService.setEmailVerified(user.getId());
        return ResponseEntity.noContent().build();
    }
}
