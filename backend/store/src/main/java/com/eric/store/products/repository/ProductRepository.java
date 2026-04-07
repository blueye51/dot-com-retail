package com.eric.store.products.repository;

import com.eric.store.products.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    @Query("""
                SELECT DISTINCT p FROM Product p
                LEFT JOIN FETCH p.brand
                LEFT JOIN FETCH p.productImages pi
                LEFT JOIN FETCH pi.file
                WHERE p.id = :id
            """)
    Optional<Product> findByIdWithImages(UUID id);

    @Modifying
    @Query("UPDATE Product p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    void incrementViewCount(@Param("id") UUID id);

    @Query("""
                SELECT p FROM Product p
                LEFT JOIN FETCH p.brand
                LEFT JOIN FETCH p.category
                WHERE p.category.id IN :categoryIds
                  AND p.id NOT IN :excludeIds
                  AND p.stock > 0
                ORDER BY p.averageRating DESC
            """)
    List<Product> findRelatedByCategoryIds(
            @Param("categoryIds") List<UUID> categoryIds,
            @Param("excludeIds") List<UUID> excludeIds,
            Pageable pageable
    );

    @Query("""
                SELECT p FROM Product p
                LEFT JOIN FETCH p.brand
                LEFT JOIN FETCH p.category
                WHERE p.id NOT IN :excludeIds
                  AND p.stock > 0
                ORDER BY p.averageRating DESC
            """)
    List<Product> findTopRatedExcluding(
            @Param("excludeIds") List<UUID> excludeIds,
            Pageable pageable
    );
}
