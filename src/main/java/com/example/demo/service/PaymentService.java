package com.example.demo.service;

import com.example.demo.Entity.Payment;
import com.example.demo.repository.PaymentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    /**
     * 전체 결제 내역 조회 (최신순)
     */
    @Transactional(readOnly = true)
    public List<Payment> findAllPayments() {
        return paymentRepository.findAllByOrderByIdDesc();
    }

    /**
     * 결제 취소 (토스페이먼츠 API 연동)
     * @param paymentId DB 내 결제 고유 ID
     * @param cancelReason 취소 사유
     */
    @Value("${TOSS_SKEY}")
    private String secretKeytoss;
    @Transactional
    public void cancelPayment(Long paymentId, String cancelReason) {
        // 1. DB에서 결제 정보 조회
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 결제 내역이 존재하지 않습니다."));

        // 이미 취소된 경우 예외 처리
        if ("CANCELED".equals(payment.getStatus())) {
            throw new IllegalStateException("이미 취소된 결제입니다.");
        }

        // 2. 토스페이먼츠 API 호출 설정
        // 주의: 실제 서비스 시에는 Secret Key를 Base64로 인코딩하여 환경변수에서 관리하는 것이 안전합니다.
        String secretKey = secretKeytoss; // 토스에서 발급받은 시크릿 키 입력
        String url = "https://api.tosspayments.com/v1/payments/" + payment.getPaymentKey() + "/cancel";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(secretKey, ""); // 비밀번호 자리는 비워둠
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("cancelReason", cancelReason);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        try {
            // 3. API 호출 (POST 요청)
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(url, entity, JsonNode.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                // 4. API 성공 시 DB 상태 업데이트
                payment.setStatus("CANCELED");
                paymentRepository.save(payment);
            } else {
                throw new RuntimeException("토스 API 응답 오류: " + response.getStatusCode());
            }
        } catch (Exception e) {
            // 통신 장애 또는 API 에러 발생 시 처리
            throw new RuntimeException("결제 취소 통신 중 에러가 발생했습니다: " + e.getMessage());
        }
    }
    public boolean checkActiveTicket(String email) {
        // 가장 최근 결제 건이 'DONE'인 경우 true 리턴
        return paymentRepository.findTopByMemberEmailOrderByIdDesc(email)
                .map(pay -> "DONE".equals(pay.getStatus()) || "PAID".equals(pay.getStatus()))
                .orElse(false);
    }
    /**
     * 타로 서비스 사용 상태를 업데이트합니다.
     * @param paymentId 결제 고유 ID
     * @param status 변경할 상태 ("USED" 등)
     */
    @Transactional
    public void updateTarotStatus(Long paymentId, String status) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 결제 내역입니다. ID: " + paymentId));

        // 상태 변경
        payment.setTarotStatus(status);

        // JPA의 더티 체킹(Dirty Checking)으로 인해
        // @Transactional이 붙어있으면 save()를 명시하지 않아도 DB에 반영됩니다.
    }
    /**
     * 대시보드용: 총 매출 합계 계산 (DONE, PAID 상태)
     */
    @Transactional(readOnly = true)
    public Long getTotalRevenue() {
        Long sum = paymentRepository.sumTotalRevenue();
        return sum != null ? sum : 0L;
    }

    /**
     * 대시보드용: 완료된 총 결제 건수
     */
    @Transactional(readOnly = true)
    public long getSuccessPaymentCount() {
        return paymentRepository.countByStatusIn(Arrays.asList("DONE", "PAID"));
    }
}
