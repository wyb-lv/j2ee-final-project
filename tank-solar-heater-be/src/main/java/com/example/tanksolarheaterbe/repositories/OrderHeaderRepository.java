package com.example.tanksolarheaterbe.repositories;

import com.example.tanksolarheaterbe.entities.OrderHeader;
import com.example.tanksolarheaterbe.entities.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderHeaderRepository extends JpaRepository<OrderHeader, Integer> {

    List<OrderHeader> findByCustomerId(Integer customerId);

    List<OrderHeader> findByStatus(OrderStatus status);
}

