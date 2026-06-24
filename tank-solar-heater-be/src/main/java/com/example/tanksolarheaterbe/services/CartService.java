package com.example.tanksolarheaterbe.services;

import com.example.tanksolarheaterbe.dto.CartLineResponse;
import com.example.tanksolarheaterbe.dto.CartResponse;
import com.example.tanksolarheaterbe.entities.Product;
import com.example.tanksolarheaterbe.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Cart module. The cart (productId -> quantity) is supplied by the caller from
 * the client cookie; this service mutates that map in place and prices it
 * against current product data. Nothing is stored on the server.
 */
@Service
@RequiredArgsConstructor
public class CartService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final ProductRepository productRepository;

    public CartResponse getCart(Map<Integer, Integer> items) {
        return build(items);
    }

    public CartResponse addItem(Map<Integer, Integer> items, Integer productId, int quantity) {
        productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
        items.merge(productId, quantity, Integer::sum);
        if (items.getOrDefault(productId, 0) <= 0) {
            items.remove(productId);
        }
        return build(items);
    }

    public CartResponse setItem(Map<Integer, Integer> items, Integer productId, int quantity) {
        if (quantity <= 0) {
            items.remove(productId);
        } else {
            items.put(productId, quantity);
        }
        return build(items);
    }

    public CartResponse removeItem(Map<Integer, Integer> items, Integer productId) {
        items.remove(productId);
        return build(items);
    }

    /** Builds a priced response from the given cart map. */
    private CartResponse build(Map<Integer, Integer> items) {

        List<CartLineResponse> lines = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;
        int itemCount = 0;

        for (Map.Entry<Integer, Integer> entry : items.entrySet()) {
            Product product = productRepository.findById(entry.getKey()).orElse(null);
            if (product == null) {
                continue; // product was removed from the catalog; skip it
            }
            int qty = entry.getValue();

            BigDecimal discount = product.getDiscount() != null ? product.getDiscount() : BigDecimal.ZERO;
            BigDecimal factor = BigDecimal.ONE.subtract(discount.divide(HUNDRED, 4, RoundingMode.HALF_UP));
            BigDecimal finalPrice = product.getPrice().multiply(factor).setScale(2, RoundingMode.HALF_UP);

            BigDecimal q = BigDecimal.valueOf(qty);
            BigDecimal lineTotal = finalPrice.multiply(q);

            subtotal = subtotal.add(product.getPrice().multiply(q));
            total = total.add(lineTotal);
            itemCount += qty;

            lines.add(CartLineResponse.builder()
                    .productId(product.getId())
                    .name(product.getName())
                    .imageUrl(product.getImageUrl())
                    .price(product.getPrice())
                    .discount(discount)
                    .finalPrice(finalPrice)
                    .quantity(qty)
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
