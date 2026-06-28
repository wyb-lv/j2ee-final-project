package com.example.tanksolarheaterbe.services;

import com.example.tanksolarheaterbe.dto.BrandRequest;
import com.example.tanksolarheaterbe.dto.BrandResponse;
import com.example.tanksolarheaterbe.entities.Brand;
import com.example.tanksolarheaterbe.repositories.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;

    public List<BrandResponse> getAll() {
        return brandRepository.findAll().stream().map(this::toResponse).toList();
    }

    public BrandResponse getById(Integer id) {
        return brandRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Brand not found: " + id));
    }

    public BrandResponse create(BrandRequest request) {
        Brand brand = new Brand();
        brand.setName(request.getName());
        return toResponse(brandRepository.save(brand));
    }

    public BrandResponse update(Integer id, BrandRequest request) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Brand not found: " + id));
        brand.setName(request.getName());
        return toResponse(brandRepository.save(brand));
    }

    public void delete(Integer id) {
        brandRepository.deleteById(id);
    }

    private BrandResponse toResponse(Brand brand) {
        return BrandResponse.builder()
                .id(brand.getId())
                .name(brand.getName())
                .build();
    }
}
