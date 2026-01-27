package com.eric.store.products.service;

import com.eric.store.common.exceptions.NotFoundException;
import com.eric.store.files.entity.FileEntity;
import com.eric.store.files.repository.FileRepository;
import com.eric.store.products.dto.ImageCreate;
import com.eric.store.products.dto.ImageResponse;
import com.eric.store.products.entity.Product;
import com.eric.store.products.entity.ProductImage;
import com.eric.store.products.repository.ProductImageRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductImageService {
    private final ProductImageRepository productImageRepository;
    private final FileRepository fileRepository;

    public ProductImage create(ImageCreate imageCreate, Product product) {
        FileEntity file = fileRepository.findById(imageCreate.fileKey())
                .orElseThrow(() -> new NotFoundException("FileEntity", imageCreate.fileKey()));

        ProductImage productImage = new ProductImage(
                file,
                imageCreate.sortOrder()
        );

        product.addImage(productImage);

        return productImageRepository.save(productImage);
    }

    private List<ProductImage> getImagesByProductIdAsc(Product product) {
        return productImageRepository.findAllByProductOrderBySortOrderAsc(product);
    }
}
