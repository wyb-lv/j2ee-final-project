package com.example.tanksolarheaterbe.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "\"Product\"")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    @Size(max = 1000)
    @Column(name = "description", length = 1000)
    private String description;

    @NotNull
    @PositiveOrZero
    @Column(name = "price", nullable = false, precision = 18)
    private BigDecimal price;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    @ColumnDefault("0")
    @Column(name = "discount", nullable = false, precision = 5, scale = 2)
    private BigDecimal discount;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "\"categoryId\"", nullable = false)
    private Category category;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "\"brandId\"", nullable = false)
    private Brand brand;

    @NotBlank
    @Column(name = "\"imageUrl\"", nullable = false)
    private String imageUrl;


}