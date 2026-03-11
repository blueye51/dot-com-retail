package com.eric.store.auth.controller;

import com.eric.store.auth.service.EmailVerificationService;
import com.eric.store.user.entity.User;
import com.eric.store.user.service.UserService;
import lombok.RequiredArgsConstructor;
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

    public record VerifyRequest(String code) {}

    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendVerificationCode(@AuthenticationPrincipal UUID userId) {
        User user = userService.findById(userId);
        String email = user.getEmail();
        verificationService.sendCode(email);
        return ResponseEntity.ok().body(Map.of("email",  email));
    }

    @PostMapping("/verify")
    public ResponseEntity<Void> verifyEmail(
            @AuthenticationPrincipal UUID userId,
            @RequestBody VerifyRequest req) {
        User user = userService.findById(userId);
        verificationService.verifyCode(user.getEmail(), req.code());
        userService.setEmailVerified(userId);
        return ResponseEntity.noContent().build();
    }

}
