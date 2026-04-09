package com.eric.store.user.controller;

import com.eric.store.orders.dto.AdminOrderSummary;
import com.eric.store.orders.dto.OrderResponse;
import com.eric.store.orders.dto.UpdateStatusRequest;
import com.eric.store.orders.service.OrderService;
import com.eric.store.products.dto.BulkUploadResult;
import com.eric.store.products.service.BulkProductService;
import com.eric.store.ratings.dto.RatingDto;
import com.eric.store.ratings.service.RatingService;
import com.eric.store.user.dto.AdminUserDto;
import com.eric.store.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final UserService userService;
    private final OrderService orderService;
    private final BulkProductService bulkProductService;
    private final RatingService ratingService;

    // ── Bulk upload ──

    @PostMapping("/products/bulk")
    public ResponseEntity<BulkUploadResult> bulkUpload(@RequestParam("file") MultipartFile file) throws Exception {
        return ResponseEntity.ok(bulkProductService.upload(file));
    }

    // ── Review moderation ──

    @GetMapping("/reviews")
    public ResponseEntity<Page<RatingDto>> getAllReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ratingService.getAllReviewsForAdmin(page, size));
    }

    @PostMapping("/reviews/{id}/toggle-hidden")
    public ResponseEntity<RatingDto> toggleReviewHidden(@PathVariable UUID id) {
        return ResponseEntity.ok(ratingService.toggleHidden(id));
    }

    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable UUID id) {
        ratingService.adminDelete(id);
        return ResponseEntity.noContent().build();
    }

    // ── User management ──

    @GetMapping("/users")
    public ResponseEntity<Page<AdminUserDto>> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(userService.searchUsers(search, page, size));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<AdminUserDto> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getAdminUserById(id));
    }

    @PostMapping("/users/{id}/grant-admin")
    public ResponseEntity<AdminUserDto> grantAdmin(@PathVariable UUID id) {
        userService.promoteToAdmin(id);
        return ResponseEntity.ok(userService.getAdminUserById(id));
    }

    @PostMapping("/users/{id}/revoke-admin")
    public ResponseEntity<AdminUserDto> revokeAdmin(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.revokeAdmin(id));
    }

    @GetMapping("/orders")
    public ResponseEntity<Page<AdminOrderSummary>> getAllOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        OffsetDateTime fromDt = from != null ? from.atStartOfDay().atOffset(ZoneOffset.UTC) : null;
        OffsetDateTime toDt = to != null ? to.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC) : null;
        return ResponseEntity.ok(orderService.getAllOrders(status, fromDt, toDt, page, size));
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.getByIdAdmin(orderId));
    }

    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable UUID orderId,
            @Valid @RequestBody UpdateStatusRequest req) {
        return ResponseEntity.ok(orderService.updateStatus(orderId, req.status()));
    }
}
