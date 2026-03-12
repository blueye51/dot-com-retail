package com.eric.store.brands.service;

import com.eric.store.brands.mapper.BrandMapper;
import com.eric.store.common.exceptions.NotFoundException;
import com.eric.store.brands.dto.BrandResponse;
import com.eric.store.brands.dto.BrandRequest;
import com.eric.store.brands.entity.Brand;
import com.eric.store.brands.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BrandService {
    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;

    public Brand create(BrandRequest req) {
        Brand brand = new Brand();
        brand.setName(req.name());
        return brandRepository.save(brand);
    }

    public List<BrandResponse> getAllBrands() {
        List<Brand> brands = brandRepository.findAll();
        return brandMapper.mapBrands(brands);
    }

    public Brand findById(UUID id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Brand not found", id));
    }
}
