package com.example.tanksolarheaterbe.repositories;

import com.example.tanksolarheaterbe.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    List<Product> findByCategoryId(Integer categoryId);

    List<Product> findByNameContainingIgnoreCase(String keyword);

    List<Product> findByPriceBetween(BigDecimal min, BigDecimal max);

    Page<Product> findAll(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:categoryId IS NULL OR p.category.id = :categoryId)")
    Page<Product> findFilteredProducts(@Param("keyword") String keyword,
                                       @Param("categoryId") Integer categoryId,
                                       Pageable pageable);
}
