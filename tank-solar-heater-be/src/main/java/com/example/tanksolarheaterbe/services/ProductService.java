package com.example.tanksolarheaterbe.services;

import com.example.tanksolarheaterbe.dto.ProductRequest;
import com.example.tanksolarheaterbe.dto.ProductResponse;
import com.example.tanksolarheaterbe.entities.Brand;
import com.example.tanksolarheaterbe.entities.Category;
import com.example.tanksolarheaterbe.entities.Product;
import com.example.tanksolarheaterbe.repositories.BrandRepository;
import com.example.tanksolarheaterbe.repositories.CategoryRepository;
import com.example.tanksolarheaterbe.repositories.ProductRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    // ----- Image upload config -----
    @Value("${app.upload.dir}")
    private String uploadDir;

    private Path uploadRoot;

    @PostConstruct
    void initUploadDir() {
        uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadRoot);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create upload directory: " + uploadRoot, e);
        }
    }

    public String storeImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No file uploaded");
        }

        List<String> imageTypes = Arrays.asList("png", "jpg", "jpeg", "svg", "webp");

        String filename = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "image" : file.getOriginalFilename());

        String extension = StringUtils.getFilenameExtension(filename);

        if (extension == null || !imageTypes.contains(extension.toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only image files are allowed");
        }

        Path target = uploadRoot.resolve(filename).normalize();

        // Guard against path traversal escaping the upload root.
        if (!target.getParent().equals(uploadRoot)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file name");
        }

        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store file", e);
        }

        // Persist only the bare filename; the frontend resolves it against the
        // public images path when rendering.
        return filename;
    }

    public List<ProductResponse> getAll() {
        return productRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public Page<ProductResponse> getProductsPaged(Pageable pageable) {
        return productRepository.findAll(pageable).map(this::mapToResponse);
    }

    // ----- One method per filter (each exposed as its own API endpoint) -----

    public Page<ProductResponse> searchByKeyword(String keyword, Pageable pageable) {
        return productRepository.searchByKeyword(keyword, pageable).map(this::mapToResponse);
    }

    public Page<ProductResponse> getByCategory(Integer categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable).map(this::mapToResponse);
    }

    public Page<ProductResponse> getByBrand(Integer brandId, Pageable pageable) {
        return productRepository.findByBrandId(brandId, pageable).map(this::mapToResponse);
    }

    public Page<ProductResponse> getByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        // Treat missing bounds as 0 / very large so a one-sided range still works.
        BigDecimal min = minPrice != null ? minPrice : BigDecimal.ZERO;
        BigDecimal max = maxPrice != null ? maxPrice : new BigDecimal("999999999999");
        return productRepository.findByPriceBetween(min, max, pageable).map(this::mapToResponse);
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
                .finalPrice(finalPrice(product))
                .imageUrl(product.getImageUrl())
                .categoryId(category != null ? category.getId() : null)
                .categoryName(category != null ? category.getName() : null)
                .brandId(brand != null ? brand.getId() : null)
                .brandName(brand != null ? brand.getName() : null)
                .build();
    }

    /** Unit price after the per-product percentage discount: price * (1 - discount/100). */
    private BigDecimal finalPrice(Product product) {
        BigDecimal price = product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;
        BigDecimal discount = product.getDiscount() != null ? product.getDiscount() : BigDecimal.ZERO;
        BigDecimal factor = BigDecimal.ONE.subtract(discount.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
        return price.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }
}
