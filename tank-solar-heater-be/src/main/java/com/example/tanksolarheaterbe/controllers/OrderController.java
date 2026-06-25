package com.example.tanksolarheaterbe.controllers;

import com.example.tanksolarheaterbe.dto.OrderRequest;
import com.example.tanksolarheaterbe.dto.OrderResponse;
import com.example.tanksolarheaterbe.services.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @PostMapping("/{userId}")
    public ResponseEntity<OrderResponse> createOrder(@PathVariable Integer userId,
                                                     @Valid @RequestBody OrderRequest request) {
        return ResponseEntity.ok(orderService.createOrder(userId, request));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<OrderResponse>> getUserOrders(@PathVariable Integer userId) {
        return ResponseEntity.ok(orderService.getUserOrders(userId));
    }
}
