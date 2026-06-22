package com.example.tanksolarheaterbe.services;

import com.example.tanksolarheaterbe.dto.CartLineResponse;
import com.example.tanksolarheaterbe.dto.CartRequest;
import com.example.tanksolarheaterbe.dto.CartResponse;
import com.example.tanksolarheaterbe.entities.Product;
import com.example.tanksolarheaterbe.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Cart module. The cart lives in the browser's localStorage; this service is
 * stateless and only prices/validates a cart snapshot against current product
 * data. It never reads from or writes to a cart table in the database.
 */
@Service
@RequiredArgsConstructor
public class CartService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final ProductRepository productRepository;

    public CartResponse priceCart(CartRequest request) {

        List<CartLineResponse> lines = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;
        int itemCount = 0;

        for (var item : request.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + item.getProductId()));

            BigDecimal discount = product.getDiscount() != null ? product.getDiscount() : BigDecimal.ZERO;
            BigDecimal factor = BigDecimal.ONE.subtract(discount.divide(HUNDRED, 4, RoundingMode.HALF_UP));
            BigDecimal finalPrice = product.getPrice().multiply(factor).setScale(2, RoundingMode.HALF_UP);

            BigDecimal qty = BigDecimal.valueOf(item.getQuantity());
            BigDecimal lineTotal = finalPrice.multiply(qty);
            BigDecimal lineSubtotal = product.getPrice().multiply(qty);

            subtotal = subtotal.add(lineSubtotal);
            total = total.add(lineTotal);
            itemCount += item.getQuantity();

            lines.add(CartLineResponse.builder()
                    .productId(product.getId())
                    .name(product.getName())
                    .imageUrl(product.getImageUrl())
                    .price(product.getPrice())
                    .discount(discount)
                    .finalPrice(finalPrice)
                    .quantity(item.getQuantity())
                    .lineTotal(lineTotal)
                    .build());
        }

        return CartResponse.builder()
                .lines(lines)
                .itemCount(itemCount)
                .subtotal(subtotal.setScale(2, RoundingMode.HALF_UP))
                .discountTotal(subtotal.subtract(total).setScale(2, RoundingMode.HALF_UP))
                .total(total.setScale(2, RoundingMode.HALF_UP))
                .build();
    }
}
