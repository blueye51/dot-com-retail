package com.eric.store.auth.controller;

import com.eric.store.auth.service.PasswordResetService;
import com.eric.store.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/password-reset")
@RequiredArgsConstructor
public class PasswordResetController {
    private final PasswordResetService passwordResetService;
    private final UserService userService;

    @Value("${app.frontend-base-url}")
    private String frontendBaseUrl;

    public record SendRequest(String email) {}
    public record ResetRequest(String token, String newPassword) {}

    @PostMapping("/send")
    public ResponseEntity<Void> sendResetLink(@RequestBody SendRequest req) {
        // Always return 204 even if email doesn't exist, to prevent email enumeration
        try {
            userService.findByEmail(req.email());
            passwordResetService.sendResetLink(req.email(), frontendBaseUrl);
        } catch (Exception ignored) {}
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset")
    public ResponseEntity<Void> resetPassword(@RequestBody ResetRequest req) {
        String email = passwordResetService.verifyToken(req.token());
        userService.changePassword(email, req.newPassword());
        return ResponseEntity.noContent().build();
    }
}
