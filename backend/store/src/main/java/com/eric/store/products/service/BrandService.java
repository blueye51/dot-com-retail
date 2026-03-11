package com.eric.store.products.service;

import com.eric.store.common.exceptions.NotFoundException;
import com.eric.store.products.dto.BrandResponse;
import com.eric.store.products.dto.BrandRequest;
import com.eric.store.products.entity.Brand;
import com.eric.store.products.mapper.ProductMapper;
import com.eric.store.products.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BrandService {
    private final BrandRepository brandRepository;
    private final ProductMapper productMapper;

    public Brand create(BrandRequest req) {
        Brand brand = new Brand();
        brand.setName(req.name());
        return brandRepository.save(brand);
    }

    public List<BrandResponse> getAllBrands() {
        List<Brand> brands = brandRepository.findAll();
        return productMapper.mapBrands(brands);
    }

    public Brand findById(UUID id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Brand not found", id));
    }
}
