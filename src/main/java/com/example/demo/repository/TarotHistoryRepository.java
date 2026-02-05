package com.example.demo.repository;
import com.example.demo.Entity.Member;
import com.example.demo.Entity.TarotHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TarotHistoryRepository extends JpaRepository<TarotHistory, Long> {
    // payment_id로 조회
    Optional<TarotHistory> findByPaymentId(Long paymentId);
    /**
     * 특정 회원의 타로 복채 기록(결과 보관함)을 최신순으로 조회합니다.
     * @param member 회원 엔티티
     * @return 타로 기록 리스트
     */
    List<TarotHistory> findAllByMemberOrderByCreatedAtDesc(Member member);

    /**
     * (선택 사항) 특정 기간 내의 기록만 조회하거나
     * 특정 회원의 가장 최근 뽑은 기록 1건만 가져올 때 사용 가능합니다.
     */
    List<TarotHistory> findTop5ByMemberOrderByCreatedAtDesc(Member member);
}