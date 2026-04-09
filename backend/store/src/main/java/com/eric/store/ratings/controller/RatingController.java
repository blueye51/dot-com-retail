package com.eric.store.ratings.controller;

import com.eric.store.ratings.dto.RatingDto;
import com.eric.store.ratings.dto.RatingRequest;
import com.eric.store.ratings.service.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {
    private final RatingService ratingService;

    @PostMapping
    public ResponseEntity<Map<String, UUID>> addRating(@Valid @RequestBody RatingRequest req, @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(Map.of("ratingId", ratingService.create(req, userId).getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RatingDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ratingService.getById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RatingDto>> getByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(ratingService.getByUserId(userId));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<RatingDto>> getByProductId(@PathVariable UUID productId) {
        return ResponseEntity.ok(ratingService.getByProductId(productId));
    }

    @GetMapping("/product/{productId}/reviews")
    public ResponseEntity<List<RatingDto>> getReviews(@PathVariable UUID productId, @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(ratingService.getReviewsByProductId(productId, userId));
    }

    @PostMapping("/{id}/helpful")
    public ResponseEntity<RatingDto> voteHelpful(@PathVariable UUID id, @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(ratingService.voteHelpful(id, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RatingDto> update(@PathVariable UUID id, @Valid @RequestBody RatingRequest req, @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(ratingService.update(id, req, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, @AuthenticationPrincipal UUID userId) {
        ratingService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}
