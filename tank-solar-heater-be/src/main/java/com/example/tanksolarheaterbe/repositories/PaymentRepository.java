package com.example.tanksolarheaterbe.repositories;

import com.example.tanksolarheaterbe.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderHeaderId(Integer orderHeaderId);

    List<Payment> findByPaymentStatus(String paymentStatus);
}
