package com.eric.store.products.repository;

import com.eric.store.products.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    Page<Product> findAll(Pageable pageable);

    @Query("""
              SELECT p
              FROM Product p
              WHERE (:categoryId IS NULL OR p.category.id = :categoryId)
            """)
    Page<Product> searchNoQuery(@Param("categoryId") UUID categoryId, Pageable pageable);

    @Query("""
              SELECT p
              FROM Product p
              WHERE (:categoryId IS NULL OR p.category.id = :categoryId)
                AND p.name ILIKE :pattern
            """)
    Page<Product> searchWithQuery(@Param("pattern") String pattern,
                                  @Param("categoryId") UUID categoryId,
                                  Pageable pageable);


    @Query("""
                SELECT DISTINCT p FROM Product p
                LEFT JOIN FETCH p.productImages pi
                LEFT JOIN FETCH pi.file
                WHERE p.id = :id
            """)
    Optional<Product> findByIdWithImages(UUID id);
}
