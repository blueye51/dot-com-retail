package com.eric.store.brands.service;

import com.eric.store.brands.mapper.BrandMapper;
import com.eric.store.common.exceptions.NotFoundException;
import com.eric.store.brands.dto.BrandResponse;
import com.eric.store.brands.dto.BrandRequest;
import com.eric.store.brands.entity.Brand;
import com.eric.store.brands.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BrandService {
    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;

    @Transactional
    public Brand create(BrandRequest req) {
        return brandRepository.save(new Brand(req.name()));
    }

    @Transactional(readOnly = true)
    public List<BrandResponse> getAllBrands() {
        List<Brand> brands = brandRepository.findAll();
        return brandMapper.mapBrands(brands);
    }

    @Transactional(readOnly = true)
    public Brand findById(UUID id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Brand not found", id));
    }
}
