package com.eric.store.products.repository;

import com.eric.store.products.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    Page<Product> findAll(Pageable pageable);

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Product> findByCategoryId(UUID categoryId, Pageable pageable);

    Page<Product> findByCategoryIdAndNameContainingIgnoreCase(UUID categoryId, String name, Pageable pageable);

    @Query("""
    SELECT DISTINCT p FROM Product p
    LEFT JOIN FETCH p.productImages pi
    LEFT JOIN FETCH pi.file
    WHERE p.id = :id
""")
    Optional<Product> findByIdWithImages(UUID id);
}
