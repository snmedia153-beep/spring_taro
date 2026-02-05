package com.example.demo.repository;

import com.example.demo.Entity.Payment;
import com.example.demo.Entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // ✅ ID(PK)를 기준으로 내림차순(최신순) 정렬하여 가져옵니다.
    List<Payment> findAllByOrderByIdDesc();
    Optional<Payment> findByOrderId(String orderId);
    List<Payment> findByMemberId(Long memberId);
    // DONE 또는 PAID 상태인 결제 금액의 합계 (상태가 여러 개이므로 @Query 사용이 편합니다)
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status IN ('DONE', 'PAID')")
    Long sumTotalRevenue();
    // 완료된 총 결제 건수
    long countByStatusIn(List<String> statuses);
    Optional<Payment> findTopByMemberEmailOrderByIdDesc(String email);
}