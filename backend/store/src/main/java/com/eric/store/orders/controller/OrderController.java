package com.eric.store.orders.controller;

import com.eric.store.orders.dto.CheckoutRequest;
import com.eric.store.orders.dto.CheckoutResponse;
import com.eric.store.orders.dto.OrderResponse;
import com.eric.store.orders.dto.OrderSummary;
import jakarta.validation.Valid;
import com.eric.store.orders.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponse> checkout(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody CheckoutRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.checkout(userId, request));
    }

    @GetMapping
    public ResponseEntity<Page<OrderSummary>> getOrderHistory(
            @AuthenticationPrincipal UUID userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        OffsetDateTime fromDt = from != null ? from.atStartOfDay().atOffset(ZoneOffset.UTC) : null;
        OffsetDateTime toDt = to != null ? to.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC) : null;
        return ResponseEntity.ok(orderService.getOrderHistory(userId, status, fromDt, toDt, page, size));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getById(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(orderService.getById(orderId, userId));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancel(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(orderService.cancel(orderId, userId));
    }
}
