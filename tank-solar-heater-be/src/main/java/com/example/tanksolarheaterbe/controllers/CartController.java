package com.example.tanksolarheaterbe.controllers;

import com.example.tanksolarheaterbe.dto.CartRequest;
import com.example.tanksolarheaterbe.dto.CartResponse;
import com.example.tanksolarheaterbe.services.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * Prices a cart snapshot held in the client's localStorage.
     * Stateless: nothing is persisted server-side.
     */
    @PostMapping
    public ResponseEntity<CartResponse> price(@Valid @RequestBody CartRequest request) {
        return ResponseEntity.ok(cartService.priceCart(request));
    }
}
