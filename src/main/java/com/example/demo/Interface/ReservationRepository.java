package com.example.demo.Interface;

import com.example.demo.Entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    // 주문 ID로 예약 내역을 찾는 메서드 (결제 승인 시 필요)
    Optional<Reservation> findByOrderId(String orderId);

    List<Reservation> findByMemberId(Long memberId);
}