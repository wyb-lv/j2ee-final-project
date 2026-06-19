package com.example.tanksolarheaterbe.services;

import com.example.tanksolarheaterbe.dto.ProductRequest;
import com.example.tanksolarheaterbe.dto.ProductResponse;
import com.example.tanksolarheaterbe.entities.Brand;
import com.example.tanksolarheaterbe.entities.Category;
import com.example.tanksolarheaterbe.entities.Product;
import com.example.tanksolarheaterbe.repositories.BrandRepository;
import com.example.tanksolarheaterbe.repositories.CategoryRepository;
import com.example.tanksolarheaterbe.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    public List<ProductResponse> getAll() {
        return productRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public Page<ProductResponse> getProductsPaged(Pageable pageable) {
        return productRepository.findAll(pageable).map(this::mapToResponse);
    }

    public Page<ProductResponse> getFilteredProducts(String keyword, Integer categoryId, Pageable pageable) {
        return productRepository.findFilteredProducts(keyword, categoryId, pageable)
                .map(this::mapToResponse);
    }

    public ProductResponse getById(Integer id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        return mapToResponse(product);
    }

    public ProductResponse create(ProductRequest request) {

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new RuntimeException("Brand not found"));

        Product product = new Product();
        applyRequest(product, request, category, brand);

        return mapToResponse(productRepository.save(product));
    }

    public ProductResponse update(Integer id, ProductRequest request) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new RuntimeException("Brand not found"));

        applyRequest(product, request, category, brand);

        return mapToResponse(productRepository.save(product));
    }

    public void delete(Integer id) {
        productRepository.deleteById(id);
    }

    private void applyRequest(Product product, ProductRequest request,
                              Category category, Brand brand) {
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setDiscount(request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO);
        product.setImageUrl(request.getImageUrl());
        product.setCategory(category);
        product.setBrand(brand);
    }

    private ProductResponse mapToResponse(Product product) {

        Category category = product.getCategory();
        Brand brand = product.getBrand();

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .discount(product.getDiscount())
                .imageUrl(product.getImageUrl())
                .categoryId(category != null ? category.getId() : null)
                .categoryName(category != null ? category.getName() : null)
                .brandId(brand != null ? brand.getId() : null)
                .brandName(brand != null ? brand.getName() : null)
                .build();
    }
}
