package com.example.demo.controller;

import com.example.demo.Entity.Member;
import com.example.demo.Entity.Payment;
import com.example.demo.Entity.Reservation;
import com.example.demo.Interface.ReservationRepository;
import com.example.demo.Security.PrincipalDetails;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.service.NotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PaymentController {

    @Value("${TOSS_SKEY}")
    private String secretKey;

    private final NotificationService notificationService;
    private final PaymentRepository paymentRepository;

    public PaymentController(PaymentRepository paymentRepository, NotificationService notificationService ) {
        this.paymentRepository = paymentRepository;
        this.notificationService = notificationService;
    }

    @GetMapping("/payment/success")
    public String paymentSuccess(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam Long amount,
            Model model) {

        // 토스에서 보낸 쿼리 파라미터를 로그로 확인
        System.out.println("paymentKey = " + paymentKey);
        System.out.println("orderId = " + orderId);
        System.out.println("amount = " + amount);

        // 비즈니스 로직: DB에 결제 정보 저장 등

        model.addAttribute("paymentKey", paymentKey);
        model.addAttribute("orderId", orderId);
        model.addAttribute("amount", amount);

        return "payment/success"; // success.html 페이지로 이동
    }
    @GetMapping("/payment/fail")
    public String paymentFail(@RequestParam String code,
                              @RequestParam String message,
                              @RequestParam String orderId,
                              Model model) {

        // 에러 코드와 메시지를 모델에 담아 화면에 출력합니다.
        model.addAttribute("code", code);
        model.addAttribute("message", message);
        model.addAttribute("orderId", orderId);

        return "payment/fail"; // templates/payment/fail.html 을 찾아감
    }@PostMapping("/api/payments/toss/confirm")
    @ResponseBody
    public ResponseEntity<String> confirmPayment(
            @RequestBody Map<String, String> paymentData,
            @AuthenticationPrincipal PrincipalDetails principalDetails) { // ✅ 현재 로그인 유저 정보 주입

        String paymentKey = paymentData.get("paymentKey");
        String orderId = paymentData.get("orderId");
        String amount = paymentData.get("amount");

        // 1. 토스 승인 API 호출 준비
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        String encodedKey = Base64.getEncoder().encodeToString((secretKey + ":").getBytes());
        headers.set("Authorization", "Basic " + encodedKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> params = new HashMap<>();
        params.put("paymentKey", paymentKey);
        params.put("orderId", orderId);
        params.put("amount", amount);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://api.tosspayments.com/v1/payments/confirm", entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                // 2. 현재 로그인한 회원 정보 가져오기
                if (principalDetails == null) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
                }
                Member currentMember = principalDetails.getMember();

                // 3. 결제 성공 시 DB에 기록 저장 (Reservation 엔티티 사용 시)
                Payment payment = new Payment();
                payment.setOrderId(orderId);
                payment.setAmount(Long.parseLong(amount));
                payment.setOrderName((String) response.getBody().get("orderName"));
                payment.setPaymentKey(paymentKey);
                payment.setStatus("PAID");

                // ✅ 가장 중요한 부분: 결제 내역에 회원 객체 연결
                payment.setMember(currentMember);

                paymentRepository.save(payment);

                // 4. 실시간 알림 발송 (로그인한 회원 ID로 발송)
                notificationService.sendNotification(currentMember.getEmail(),
                        "🔮 [" + payment.getOrderName() + "] 운명의 계약이 성사되었습니다!");

                return ResponseEntity.ok("Success");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("승인 요청 실패: " + e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unknown Error");
    }
}
