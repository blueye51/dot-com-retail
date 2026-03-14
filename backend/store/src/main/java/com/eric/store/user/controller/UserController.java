package com.eric.store.user.controller;

import com.eric.store.user.dto.UserProfile;
import com.eric.store.user.dto.UserSettingsDto;
import com.eric.store.user.entity.User;
import com.eric.store.user.entity.UserSettings;
import com.eric.store.user.mapper.UserMapper;
import com.eric.store.user.service.UserService;
import com.eric.store.common.util.Cookie;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;
    private final Cookie cookie;

    @GetMapping("/me")
    public ResponseEntity<UserProfile> me(@AuthenticationPrincipal UUID userId) {
        User user = userService.findById(userId);
        return ResponseEntity.ok(userMapper.toUserProfile(user));
    }

    @GetMapping("/me/settings")
    public ResponseEntity<UserSettingsDto> getSettings(@AuthenticationPrincipal UUID userId) {
        UserSettings settings = userService.getSettings(userId);
        return ResponseEntity.ok(userMapper.toUserSettingsDto(settings));
    }

    @PutMapping("/me/settings")
    public ResponseEntity<UserSettingsDto> updateSettings(
            @AuthenticationPrincipal UUID userId,
            @RequestBody UserSettingsDto dto
    ) {
        UserSettings settings = userService.updateSettings(userId, dto);
        return ResponseEntity.ok(userMapper.toUserSettingsDto(settings));
    }

    public record DeleteAccountRequest(@NotBlank String password) {}

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount(
            @AuthenticationPrincipal UUID userId,
            @RequestBody @Valid DeleteAccountRequest request
    ) {
        userService.deleteAccount(userId, request.password());
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie.clearRefresh().toString())
                .build();
    }
}
