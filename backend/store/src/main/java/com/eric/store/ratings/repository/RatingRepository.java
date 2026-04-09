package com.eric.store.ratings.repository;

import com.eric.store.ratings.entity.Rating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RatingRepository extends JpaRepository<Rating, UUID> {
    List<Rating> findByUserIdOrderByCreatedAtDesc(UUID userId);
    List<Rating> findByProductIdOrderByCreatedAtDesc(UUID productId);

    @Query("SELECT r FROM Rating r JOIN FETCH r.user WHERE r.product.id = :productId AND r.hidden = false AND r.comment IS NOT NULL AND r.comment <> '' ORDER BY r.helpfulCount DESC, r.createdAt DESC")
    List<Rating> findReviewsByProductId(UUID productId);

    @Query("SELECT COALESCE(AVG(r.score), 0) FROM Rating r WHERE r.product.id = :productId AND r.hidden = false")
    double averageScoreByProductId(UUID productId);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.product.id = :productId AND r.hidden = false")
    int countVisibleByProductId(@Param("productId") UUID productId);

    int countByProductId(UUID productId);

    @Query("""
                SELECT r FROM Rating r
                JOIN FETCH r.user
                JOIN FETCH r.product
                WHERE r.comment IS NOT NULL AND r.comment <> ''
                ORDER BY r.createdAt DESC
            """)
    Page<Rating> findAllReviewsForAdmin(Pageable pageable);
}
