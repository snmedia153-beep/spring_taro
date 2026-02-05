package com.example.demo.service;
import com.example.demo.Entity.Payment;
import com.example.demo.dto.TarotCard;
import com.example.demo.Entity.Member;
import com.example.demo.Entity.TarotHistory;
import com.example.demo.repository.TarotHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TarotHistoryService {

    private final TarotHistoryRepository tarotHistoryRepository;

    @Transactional(readOnly = true)
    public TarotCard getCardByPaymentId(Long paymentId) {
        // 1. DB에서 해당 결제 ID로 저장된 타로 기록 조회
        TarotHistory history = tarotHistoryRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 결제에 대한 타로 기록이 없습니다."));

        // 2. Entity를 DTO(TarotCard)로 변환하여 반환
        return new TarotCard(
                history.getId().intValue(),
                history.getCardName(),
                history.getImagePath(),
                history.getInterpretation()
        );
    }

    // 기존 saveHistory 메서드도 payment 정보를 저장하도록 수정해야 합니다.
    @Transactional
    public void saveHistory(Member member, TarotCard card, Payment payment) {
        TarotHistory history = new TarotHistory();
        history.setMember(member);
        history.setPayment(payment); // 결제 정보 연결
        history.setCardName(card.getName());
        history.setImagePath(card.getImagePath());
        history.setInterpretation(card.getInterpretation());
        tarotHistoryRepository.save(history);
    }
    public List<TarotHistory> getMemberHistory(Member member) {
        return tarotHistoryRepository.findAllByMemberOrderByCreatedAtDesc(member);
    }
}