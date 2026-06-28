package com.example.tanksolarheaterbe.controllers;

import com.example.tanksolarheaterbe.config.VnPayProperties;
import com.example.tanksolarheaterbe.dto.CheckoutRequest;
import com.example.tanksolarheaterbe.dto.CheckoutResponse;
import com.example.tanksolarheaterbe.dto.PaymentResponse;
import com.example.tanksolarheaterbe.dto.PaymentStatusRequest;
import com.example.tanksolarheaterbe.dto.VnPayConfirmation;
import com.example.tanksolarheaterbe.services.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final VnPayProperties vnPayProperties;

    @PostMapping("/checkout")
    @PreAuthorize("!hasRole('ADMIN')")
    public ResponseEntity<CheckoutResponse> checkout(@Valid @RequestBody CheckoutRequest request,
                                                     HttpServletRequest httpRequest) {
        return ResponseEntity.ok(paymentService.checkout(request, httpRequest));
    }

    /**
     * Browser landing endpoint VNPay redirects to after payment. Records the result,
     * then forwards the customer to the storefront result page.
     */
    @GetMapping("/payments/vnpay/return")
    public ResponseEntity<Void> vnpayReturn(@RequestParam Map<String, String> params) {
        VnPayConfirmation result = paymentService.confirmVnPay(params);
        URI redirect = UriComponentsBuilder.fromUriString(vnPayProperties.getFrontendReturnUrl())
                .queryParam("status", result.status())
                .queryParam("paid", result.paid())
                .queryParamIfPresent("orderId", java.util.Optional.ofNullable(result.orderId()))
                .build(true).toUri();
        return ResponseEntity.status(302).location(redirect).build();
    }

    /**
     * Server-to-server IPN callback. Returns the {RspCode, Message} acknowledgement
     * VNPay expects so it stops retrying once the result is recorded.
     */
    @GetMapping("/payments/vnpay/ipn")
    public ResponseEntity<Map<String, String>> vnpayIpn(@RequestParam Map<String, String> params) {
        VnPayConfirmation result = paymentService.confirmVnPay(params);
        return ResponseEntity.ok(switch (result.status()) {
            case SUCCESS, FAILED -> Map.of("RspCode", "00", "Message", "Confirm Success");
            case ALREADY_CONFIRMED -> Map.of("RspCode", "02", "Message", "Order already confirmed");
            case INVALID_SIGNATURE -> Map.of("RspCode", "97", "Message", "Invalid Checksum");
            case NOT_FOUND -> Map.of("RspCode", "01", "Message", "Order not found");
            case AMOUNT_MISMATCH -> Map.of("RspCode", "04", "Message", "Invalid Amount");
        });
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
