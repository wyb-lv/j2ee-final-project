package com.example.tanksolarheaterbe.controllers;

import com.example.tanksolarheaterbe.dto.CheckoutRequest;
import com.example.tanksolarheaterbe.dto.CheckoutResponse;
import com.example.tanksolarheaterbe.dto.PaymentResponse;
import com.example.tanksolarheaterbe.dto.PaymentStatusRequest;
import com.example.tanksolarheaterbe.services.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    
    @PostMapping("/checkout")
    @PreAuthorize("!hasRole('ADMIN')")
    public ResponseEntity<CheckoutResponse> checkout(@Valid @RequestBody CheckoutRequest request) {
        return ResponseEntity.ok(paymentService.checkout(request));
    }

    @GetMapping("/payments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentResponse>> getAll() {
        return ResponseEntity.ok(paymentService.getAll());
    }

    @GetMapping("/payments/{id}")
    public ResponseEntity<PaymentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getById(id));
    }

    @PutMapping("/payments/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> updateStatus(@PathVariable Long id,
                                                        @Valid @RequestBody PaymentStatusRequest request) {
        return ResponseEntity.ok(paymentService.updateStatus(id, request));
    }
}
