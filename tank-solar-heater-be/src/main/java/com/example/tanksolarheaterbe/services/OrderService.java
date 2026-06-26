package com.example.tanksolarheaterbe.services;

import com.example.tanksolarheaterbe.dto.OrderItemResponse;
import com.example.tanksolarheaterbe.dto.OrderRequest;
import com.example.tanksolarheaterbe.dto.OrderResponse;
import com.example.tanksolarheaterbe.entities.Account;
import com.example.tanksolarheaterbe.entities.OrderDetail;
import com.example.tanksolarheaterbe.entities.OrderHeader;
import com.example.tanksolarheaterbe.entities.OrderStatus;
import com.example.tanksolarheaterbe.entities.Payment;
import com.example.tanksolarheaterbe.entities.Product;
import com.example.tanksolarheaterbe.repositories.AccountRepository;
import com.example.tanksolarheaterbe.repositories.OrderDetailRepository;
import com.example.tanksolarheaterbe.repositories.OrderHeaderRepository;
import com.example.tanksolarheaterbe.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderHeaderRepository orderHeaderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public OrderResponse createOrder(Integer customerId, OrderRequest request) {

        Account customer = accountRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        OrderHeader header = placeOrder(customer, request);
        return toResponse(header);
    }

    /**
     * Persists an order (header + details) for an already-resolved account and
     * returns the saved header. Shared by the user-order and checkout flows.
     */
    @Transactional
    public OrderHeader placeOrder(Account customer, OrderRequest request) {

        OrderHeader header = new OrderHeader();
        header.setDate(LocalDate.now());
        header.setStatus(OrderStatus.PENDING);
        header.setCustomer(customer);
        header.setEmployeeId(request.getEmployeeId());
        header.setAddress(request.getAddress());

        OrderHeader savedHeader = orderHeaderRepository.save(header);

        List<OrderDetail> details = request.getItems().stream()
                .map(item -> {
                    Product product = productRepository.findById(item.getProductId())
                            .orElseThrow(() -> new RuntimeException("Product not found: " + item.getProductId()));

                    OrderDetail detail = new OrderDetail();
                    detail.setOrderHeader(savedHeader);
                    detail.setProduct(product);
                    detail.setQuantity(item.getQuantity());
                    detail.setPrice(product.getPrice());
                    detail.setDiscount(product.getDiscount() != null ? product.getDiscount() : BigDecimal.ZERO);
                    return detail;
                })
                .toList();

        orderDetailRepository.saveAll(details);

        return savedHeader;
    }

    /** Maps a persisted order header to its response, loading its details. */
    public OrderResponse toResponse(OrderHeader header) {
        return mapToResponse(header, orderDetailRepository.findByOrderHeaderId(header.getId()));
    }

    /** All orders, newest first — for the admin orders screen. */
    public List<OrderResponse> getAllOrders(OrderStatus excludeStatus) {
        return orderHeaderRepository.findAll()
                .stream()
                .filter(o -> excludeStatus == null || o.getStatus() != excludeStatus)
                .sorted((a, b) -> b.getId().compareTo(a.getId()))
                .map(this::toResponse)
                .toList();
    }

    public List<OrderResponse> getUserOrders(Integer customerId) {

        return orderHeaderRepository.findByCustomerId(customerId)
                .stream()
                .map(header -> mapToResponse(
                        header,
                        orderDetailRepository.findByOrderHeaderId(header.getId())))
                .toList();
    }

    @Transactional
    public OrderResponse updateOrderStatus(Integer orderId, OrderStatus status) {
        OrderHeader header = orderHeaderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        header.setStatus(status);
        orderHeaderRepository.save(header);
        return toResponse(header);
    }

    private OrderResponse mapToResponse(OrderHeader header, List<OrderDetail> details) {

        BigDecimal total = details.stream()
                .map(d -> d.getPrice().multiply(BigDecimal.valueOf(d.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<OrderItemResponse> items = details.stream()
                .map(d -> OrderItemResponse.builder()
                        .productId(d.getProduct().getId())
                        .productName(d.getProduct().getName())
                        .quantity(d.getQuantity())
                        .price(d.getPrice())
                        .discount(d.getDiscount())
                        .build())
                .toList();

        Payment payment = header.getPayment();

        return OrderResponse.builder()
                .id(header.getId())
                .customerId(header.getCustomer().getId())
                .customerName(header.getCustomer().getName())
                .date(header.getDate())
                .status(header.getStatus().name())
                .address(header.getAddress())
                .employeeId(header.getEmployeeId())
                .total(total)
                .paymentMethod(payment != null ? payment.getPaymentMethod() : null)
                .paymentStatus(payment != null ? payment.getPaymentStatus() : null)
                .paidAt(payment != null ? payment.getPaidAt() : null)
                .items(items)
                .build();
    }
}
