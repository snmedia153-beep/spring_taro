package com.example.demo.repository;

import com.example.demo.Entity.Payment;
import com.example.demo.Entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(String orderId);
    List<Payment> findByMemberId(Long memberId);
}