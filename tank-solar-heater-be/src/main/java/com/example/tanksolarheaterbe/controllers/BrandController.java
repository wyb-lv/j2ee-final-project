package com.example.tanksolarheaterbe.controllers;

import com.example.tanksolarheaterbe.dto.BrandRequest;
import com.example.tanksolarheaterbe.dto.BrandResponse;
import com.example.tanksolarheaterbe.services.BrandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    @GetMapping
    public ResponseEntity<List<BrandResponse>> getAll() {
        return ResponseEntity.ok(brandService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BrandResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(brandService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BrandResponse> create(@Valid @RequestBody BrandRequest request) {
        return ResponseEntity.ok(brandService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BrandResponse> update(@PathVariable Integer id,
                                                @Valid @RequestBody BrandRequest request) {
        return ResponseEntity.ok(brandService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        brandService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
