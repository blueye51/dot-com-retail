package com.eric.store.brands.mapper;

import com.eric.store.brands.dto.BrandRequest;
import com.eric.store.brands.dto.BrandResponse;
import com.eric.store.brands.entity.Brand;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BrandMapper {

    public List<BrandResponse> mapBrands(List<Brand> brands) {
        return brands.stream()
                .map(this::toBrandDto)
                .toList();
    }

    public BrandResponse toBrandDto(Brand brand) {
        return new BrandResponse(
                brand.getId(),
                brand.getName()
        );
    }

}
