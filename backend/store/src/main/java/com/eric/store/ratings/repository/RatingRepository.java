package com.eric.store.ratings.repository;

import com.eric.store.ratings.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RatingRepository extends JpaRepository<Rating, UUID> {
    List<Rating> findByUserId(UUID userId);
    List<Rating> findByProductId(UUID productId);
}
