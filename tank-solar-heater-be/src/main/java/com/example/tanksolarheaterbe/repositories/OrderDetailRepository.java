package com.example.tanksolarheaterbe.repositories;

import com.example.tanksolarheaterbe.entities.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {

    List<OrderDetail> findByOrderHeaderId(Integer orderHeaderId);

    List<OrderDetail> findByProductId(Integer productId);
}
