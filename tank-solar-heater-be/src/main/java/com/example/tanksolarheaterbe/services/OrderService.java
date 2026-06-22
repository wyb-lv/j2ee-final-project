package com.example.tanksolarheaterbe.services;

import com.example.tanksolarheaterbe.dto.OrderItemResponse;
import com.example.tanksolarheaterbe.dto.OrderRequest;
import com.example.tanksolarheaterbe.dto.OrderResponse;
import com.example.tanksolarheaterbe.entities.Account;
import com.example.tanksolarheaterbe.entities.OrderDetail;
import com.example.tanksolarheaterbe.entities.OrderHeader;
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
        header.setStatus("PENDING");
        header.setCustomer(customer);
        header.setEmployeeId(request.getEmployeeId());

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

    public List<OrderResponse> getUserOrders(Integer customerId) {

        return orderHeaderRepository.findByCustomerId(customerId)
                .stream()
                .map(header -> mapToResponse(
                        header,
                        orderDetailRepository.findByOrderHeaderId(header.getId())))
                .toList();
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

        return OrderResponse.builder()
                .id(header.getId())
                .customerId(header.getCustomer().getId())
                .date(header.getDate())
                .status(header.getStatus())
                .employeeId(header.getEmployeeId())
                .total(total)
                .items(items)
                .build();
    }
}
