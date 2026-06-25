package com.example.tanksolarheaterbe.controllers;

import com.example.tanksolarheaterbe.config.AppConfig;
import com.example.tanksolarheaterbe.dto.ProductRequest;
import com.example.tanksolarheaterbe.dto.ProductResponse;
import com.example.tanksolarheaterbe.services.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId,
            Pageable pageable) {

        // Convert 1-based frontend page to 0-based backend page
        Pageable correctedPageable = PageRequest.of(page - 1, AppConfig.PAGE_SIZE, pageable.getSort());

        Page<ProductResponse> productPage =
                productService.getFilteredProducts(keyword, categoryId, correctedPageable);

        return ResponseEntity.ok(productPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.create(request));
    }


    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        String url = productService.storeImage(file);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> update(@PathVariable Integer id,
                                                  @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
