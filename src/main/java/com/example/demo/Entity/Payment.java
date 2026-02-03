package com.example.demo.Entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String paymentKey; // 토스 결제 고유 키
    private String orderId;    // 내 시스템의 주문 ID
    private String orderName;  // 상품명
    private Long amount;       // 결제 금액
    private String method;     // 결제 수단 (카드 등)
    private String status;     // 상태 (READY, DONE, CANCELLED 등)

    @ManyToOne(fetch = FetchType.LAZY) // 다대일 관계 설정
    @JoinColumn(name = "member_id")   // DB의 member_id 컬럼과 매핑
    private Member member;            // 결제한 회원 정보

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}