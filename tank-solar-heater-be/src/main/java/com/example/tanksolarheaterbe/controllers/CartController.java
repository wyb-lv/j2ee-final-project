package com.example.tanksolarheaterbe.controllers;

import com.example.tanksolarheaterbe.dto.CartResponse;
import com.example.tanksolarheaterbe.dto.OrderItemRequest;
import com.example.tanksolarheaterbe.dto.QuantityRequest;
import com.example.tanksolarheaterbe.services.CartCookieStore;
import com.example.tanksolarheaterbe.services.CartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@PreAuthorize("!hasRole('ADMIN')")
public class CartController {

    private final CartService cartService;
    private final CartCookieStore cookieStore;

    @GetMapping
    public ResponseEntity<CartResponse> getCart(HttpServletRequest request) {
        return ResponseEntity.ok(cartService.getCart(cookieStore.read(request)));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(@Valid @RequestBody OrderItemRequest body,
                                                HttpServletRequest request,
                                                HttpServletResponse response) {
        Map<Integer, Integer> items = cookieStore.read(request);
        CartResponse cart = cartService.addItem(items, body.getProductId(), body.getQuantity());
        cookieStore.write(response, items);
        return ResponseEntity.ok(cart);
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<CartResponse> setItem(@PathVariable Integer productId,
                                                @Valid @RequestBody QuantityRequest body,
                                                HttpServletRequest request,
                                                HttpServletResponse response) {
        Map<Integer, Integer> items = cookieStore.read(request);
        CartResponse cart = cartService.setItem(items, productId, body.getQuantity());
        cookieStore.write(response, items);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponse> removeItem(@PathVariable Integer productId,
                                                   HttpServletRequest request,
                                                   HttpServletResponse response) {
        Map<Integer, Integer> items = cookieStore.read(request);
        CartResponse cart = cartService.removeItem(items, productId);
        cookieStore.write(response, items);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping
    public ResponseEntity<CartResponse> clear(HttpServletResponse response) {
        cookieStore.clear(response);
        return ResponseEntity.ok(cartService.getCart(Map.of()));
    }
}
