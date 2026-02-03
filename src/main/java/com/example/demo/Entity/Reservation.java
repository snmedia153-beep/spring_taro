package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String paymentKey; // 토스 결제 고유 키
    private String orderId;    // 토스 결제 시 사용한 주문 ID
    private String orderName;  // 상담명 (예: 연애운)
    private Long amount;       // 결제 금액
    private String status;     // 상태 (PENDING, PAID, CANCELLED)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;      // 예약한 사용자
}