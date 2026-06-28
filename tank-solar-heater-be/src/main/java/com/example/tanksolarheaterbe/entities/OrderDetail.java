package com.example.tanksolarheaterbe.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "\"OrderDetail\"")
public class OrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "\"orderHeaderId\"", nullable = false)
    private OrderHeader orderHeader;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "\"productId\"", nullable = false)
    private Product product;

    @NotNull
    @Positive
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

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


}