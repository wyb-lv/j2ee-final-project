package com.example.tanksolarheaterbe.services;

import com.example.tanksolarheaterbe.dto.*;
import com.example.tanksolarheaterbe.entities.Account;
import com.example.tanksolarheaterbe.entities.OrderDetail;
import com.example.tanksolarheaterbe.entities.OrderHeader;
import com.example.tanksolarheaterbe.entities.Payment;
import com.example.tanksolarheaterbe.repositories.AccountRepository;
import com.example.tanksolarheaterbe.repositories.OrderDetailRepository;
import com.example.tanksolarheaterbe.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final PaymentRepository paymentRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final AccountRepository accountRepository;
    private final OrderService orderService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Turns a storefront cart into a persisted order + a pending payment.
     * Works for guests: the account is looked up by email or created on the fly.
     */
    @Transactional
    public CheckoutResponse checkout(CheckoutRequest request) {

        Account customer = accountRepository.findByEmail(request.getCustomerEmail())
                .orElseGet(() -> createGuestAccount(request));

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setItems(request.getItems());

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

        return CheckoutResponse.builder()
                .order(orderResponse)
                .payment(toResponse(saved))
                .build();
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

    /** Total actually charged: price after per-item discount, summed across the order. */
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

    private Account createGuestAccount(CheckoutRequest request) {
        Account account = new Account();
        account.setEmail(request.getCustomerEmail());
        account.setName(request.getCustomerName());
        account.setPhone(request.getCustomerPhone());
        account.setAddress(request.getCustomerAddress());
        account.setRole("customer");
        account.setEnabled(true);
        // Guest checkout: a non-usable random password (the account can reset later).
        account.setPassword(passwordEncoder.encode("guest:" + request.getCustomerEmail()));
        return accountRepository.save(account);
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
