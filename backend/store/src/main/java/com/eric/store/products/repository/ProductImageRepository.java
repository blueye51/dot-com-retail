package com.eric.store.products.repository;

import com.eric.store.products.entity.Product;
import com.eric.store.products.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductImageRepository extends JpaRepository<ProductImage, UUID> {

    List<ProductImage> findAllByProductOrderBySortOrderAsc(Product product);

    Optional<ProductImage> findFirstByProductOrderBySortOrderAsc(Product product);

    @Query("""
    SELECT pi
    FROM ProductImage pi
    JOIN FETCH pi.file
    WHERE pi.sortOrder = 0
      AND pi.product.id IN :productIds
""")
    List<ProductImage> findThumbnails(@Param("productIds") List<UUID> productIds);

}
