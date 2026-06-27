package com.example.tanksolarheaterbe.controllers;

import com.example.tanksolarheaterbe.dto.CartResponse;
import com.example.tanksolarheaterbe.dto.OrderItemRequest;
import com.example.tanksolarheaterbe.dto.QuantityRequest;
import com.example.tanksolarheaterbe.services.CartCookieStore;
import com.example.tanksolarheaterbe.services.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
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
    public ResponseEntity<CartResponse> getCart(
            @CookieValue(value = CartCookieStore.COOKIE_NAME, required = false) String cartCookie) {
        return ResponseEntity.ok(cartService.getCart(cookieStore.read(cartCookie)));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(
            @Valid @RequestBody OrderItemRequest body,
            @CookieValue(value = CartCookieStore.COOKIE_NAME, required = false) String cartCookie) {
        Map<Integer, Integer> items = cookieStore.read(cartCookie);
        CartResponse cart = cartService.addItem(items, body.getProductId(), body.getQuantity());
        return withCart(cart, items);
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<CartResponse> setItem(
            @PathVariable Integer productId,
            @Valid @RequestBody QuantityRequest body,
            @CookieValue(value = CartCookieStore.COOKIE_NAME, required = false) String cartCookie) {
        Map<Integer, Integer> items = cookieStore.read(cartCookie);
        CartResponse cart = cartService.setItem(items, productId, body.getQuantity());
        return withCart(cart, items);
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponse> removeItem(
            @PathVariable Integer productId,
            @CookieValue(value = CartCookieStore.COOKIE_NAME, required = false) String cartCookie) {
        Map<Integer, Integer> items = cookieStore.read(cartCookie);
        CartResponse cart = cartService.removeItem(items, productId);
        return withCart(cart, items);
    }

    @DeleteMapping
    public ResponseEntity<CartResponse> clear() {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieStore.clearCookie().toString())
                .body(cartService.getCart(Map.of()));
    }

    /** Returns the cart body together with the refreshed cart cookie. */
    private ResponseEntity<CartResponse> withCart(CartResponse cart, Map<Integer, Integer> items) {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieStore.buildCookie(items).toString())
                .body(cart);
    }
}
