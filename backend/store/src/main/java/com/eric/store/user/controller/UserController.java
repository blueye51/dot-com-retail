package com.eric.store.user.controller;

import com.eric.store.user.dto.UserProfile;
import com.eric.store.user.entity.User;
import com.eric.store.user.mapper.UserMapper;
import com.eric.store.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping("/me")
    public ResponseEntity<UserProfile> me(@AuthenticationPrincipal UUID userId) {
        User user = userService.findById(userId);
        return ResponseEntity.ok(userMapper.toUserProfile(user));
    }

}
