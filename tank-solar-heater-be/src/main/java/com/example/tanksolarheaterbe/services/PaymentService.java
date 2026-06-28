package com.example.tanksolarheaterbe.services;

import com.example.tanksolarheaterbe.dto.*;
import com.example.tanksolarheaterbe.entities.Account;
import com.example.tanksolarheaterbe.entities.OrderDetail;
import com.example.tanksolarheaterbe.entities.OrderHeader;
import com.example.tanksolarheaterbe.entities.Payment;
import com.example.tanksolarheaterbe.entities.PaymentMethod;
import com.example.tanksolarheaterbe.repositories.AccountRepository;
import com.example.tanksolarheaterbe.repositories.OrderDetailRepository;
import com.example.tanksolarheaterbe.repositories.PaymentRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final PaymentRepository paymentRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final AccountRepository accountRepository;
    private final OrderService orderService;
    private final VnPayService vnPayService;

    @Transactional
    public CheckoutResponse checkout(CheckoutRequest request, HttpServletRequest httpRequest) {

        // Checkout requires a signed-in customer; the frontend gates /checkout behind login.
        Account customer = accountRepository.findByEmail(currentUserEmail())
                .orElseThrow(() -> new RuntimeException("You must be logged in to check out."));

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setItems(request.getItems());
        orderRequest.setAddress(request.getCustomerAddress());

        OrderHeader header = orderService.placeOrder(customer, orderRequest);
        OrderResponse orderResponse = orderService.toResponse(header);

        BigDecimal amount = computeAmount(header.getId());

        Payment payment = Payment.builder()
                .orderHeader(header)
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus("PENDING")
                .amount(amount)
                .createdAt(LocalDateTime.now())
                .build();

        Payment saved = paymentRepository.save(payment);

        // BANK_TRANSFER is settled online through VNPay: hand back a redirect URL.
        // COD has no online step, so paymentUrl stays null.
        String paymentUrl = request.getPaymentMethod() == PaymentMethod.BANK_TRANSFER
                ? vnPayService.buildPaymentUrl(saved, httpRequest)
                : null;

        return CheckoutResponse.builder()
                .order(orderResponse)
                .payment(toResponse(saved))
                .paymentUrl(paymentUrl)
                .build();
    }

    /**
     * Verifies a VNPay return/IPN callback and applies it to the matching payment.
     * Idempotent: a payment already marked PAID is reported as ALREADY_CONFIRMED.
     */
    @Transactional
    public VnPayConfirmation confirmVnPay(Map<String, String> params) {
        if (!vnPayService.isValidSignature(params)) {
            return new VnPayConfirmation(VnPayConfirmation.Status.INVALID_SIGNATURE, null, null);
        }

        Long paymentId = parsePaymentId(params.get("vnp_TxnRef"));
        Payment payment = paymentId == null
                ? null
                : paymentRepository.findById(paymentId).orElse(null);
        if (payment == null) {
            return new VnPayConfirmation(VnPayConfirmation.Status.NOT_FOUND, paymentId, null);
        }

        Integer orderId = payment.getOrderHeader().getId();

        // VNPay echoes the amount in the smallest VND unit (× 100); reject any mismatch.
        String expectedAmount = payment.getAmount()
                .multiply(BigDecimal.valueOf(100)).toBigInteger().toString();
        if (!expectedAmount.equals(params.get("vnp_Amount"))) {
            return new VnPayConfirmation(VnPayConfirmation.Status.AMOUNT_MISMATCH, paymentId, orderId);
        }

        if ("PAID".equalsIgnoreCase(payment.getPaymentStatus())) {
            return new VnPayConfirmation(VnPayConfirmation.Status.ALREADY_CONFIRMED, paymentId, orderId);
        }

        boolean success = "00".equals(params.get("vnp_ResponseCode"))
                && "00".equals(params.get("vnp_TransactionStatus"));

        payment.setTransactionId(params.get("vnp_TransactionNo"));
        if (success) {
            payment.setPaymentStatus("PAID");
            payment.setPaidAt(LocalDateTime.now());
        } else {
            payment.setPaymentStatus("FAILED");
        }
        paymentRepository.save(payment);

        return new VnPayConfirmation(
                success ? VnPayConfirmation.Status.SUCCESS : VnPayConfirmation.Status.FAILED,
                paymentId, orderId);
    }

    private Long parsePaymentId(String txnRef) {
        try {
            return txnRef == null ? null : Long.parseLong(txnRef);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public List<PaymentResponse> getAll() {
        return paymentRepository.findAll().stream().map(this::toResponse).toList();
    }

    public PaymentResponse getById(Long id) {
        return paymentRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + id));
    }

    @Transactional
    public PaymentResponse updateStatus(Long id, PaymentStatusRequest request) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + id));

        payment.setPaymentStatus(request.getStatus());
        if (request.getTransactionId() != null) {
            payment.setTransactionId(request.getTransactionId());
        }
        if ("PAID".equalsIgnoreCase(request.getStatus())) {
            payment.setPaidAt(LocalDateTime.now());
        }
        return toResponse(paymentRepository.save(payment));
    }

    private BigDecimal computeAmount(Integer orderId) {
        return orderDetailRepository.findByOrderHeaderId(orderId).stream()
                .map(this::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal lineTotal(OrderDetail detail) {
        BigDecimal discount = detail.getDiscount() != null ? detail.getDiscount() : BigDecimal.ZERO;
        BigDecimal factor = BigDecimal.ONE.subtract(discount.divide(HUNDRED, 4, RoundingMode.HALF_UP));
        return detail.getPrice()
                .multiply(factor)
                .multiply(BigDecimal.valueOf(detail.getQuantity()));
    }

    /** Email of the authenticated caller, or null when no user is signed in. */
    private String currentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return auth.getName();
    }

    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrderHeader().getId())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .amount(payment.getAmount())
                .transactionId(payment.getTransactionId())
                .createdAt(payment.getCreatedAt())
                .paidAt(payment.getPaidAt())
                .build();
    }
}
