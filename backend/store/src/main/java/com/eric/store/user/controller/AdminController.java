package com.eric.store.user.controller;

import com.eric.store.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final UserService userService;

    @PostMapping("/users/{id}/grant-admin")
    public ResponseEntity<Void> grantAdmin(@PathVariable UUID id) {
        userService.promoteToAdmin(id);
        return ResponseEntity.noContent().build();
    }
}
