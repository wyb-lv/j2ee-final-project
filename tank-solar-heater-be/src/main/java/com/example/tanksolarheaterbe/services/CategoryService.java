package com.example.tanksolarheaterbe.services;

import com.example.tanksolarheaterbe.dto.CategoryRequest;
import com.example.tanksolarheaterbe.dto.CategoryResponse;
import com.example.tanksolarheaterbe.entities.Category;
import com.example.tanksolarheaterbe.repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public CategoryResponse getById(Integer id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        return mapToResponse(category);
    }

    public CategoryResponse create(CategoryRequest request) {

        Category category = new Category();
        category.setName(request.getName());

        return mapToResponse(categoryRepository.save(category));
    }

    public CategoryResponse update(Integer id, CategoryRequest request) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        category.setName(request.getName());

        return mapToResponse(categoryRepository.save(category));
    }

    public void delete(Integer id) {
        categoryRepository.deleteById(id);
    }

    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}
