package com.eric.store.ratings.repository;

import com.eric.store.ratings.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface RatingRepository extends JpaRepository<Rating, UUID> {
    List<Rating> findByUserIdOrderByCreatedAtDesc(UUID userId);
    List<Rating> findByProductIdOrderByCreatedAtDesc(UUID productId);

    @Query("SELECT COALESCE(AVG(r.score), 0) FROM Rating r WHERE r.product.id = :productId")
    double averageScoreByProductId(UUID productId);

    int countByProductId(UUID productId);
}
