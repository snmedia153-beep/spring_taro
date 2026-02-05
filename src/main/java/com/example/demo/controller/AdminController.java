package com.example.demo.controller;

import com.example.demo.Entity.Payment;
import com.example.demo.dto.BoardDTO;
import com.example.demo.repository.BoardRepository;
import com.example.demo.repository.MemberRepository;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.service.MemberService;
import com.example.demo.service.NotificationService;
import com.example.demo.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final PaymentService paymentService;
    private final MemberService memberService;
    private final PaymentRepository paymentRepository;
    private final MemberRepository memberRepository;
    private final BoardRepository boardRepository;
    // private final PaymentService paymentService; // 결제 내역 조회용 (나중에 추가)

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // 1. 총 매출 (null 방지를 위해 기본값 0 처리)
        Long totalRevenue = paymentRepository.sumTotalRevenue();
        model.addAttribute("totalRevenue", totalRevenue != null ? totalRevenue : 0);

        // 2. 총 회원 수
        model.addAttribute("totalMembers", memberRepository.count());

        // 3. 총 게시글 수
        model.addAttribute("totalBoards", boardRepository.count());

        // 4. 총 결제 건수 (DONE, PAID 상태 기준)
        long paymentCount = paymentRepository.countByStatusIn(Arrays.asList("DONE", "PAID"));
        model.addAttribute("paymentCount", paymentCount);

        // 최근 6개월간의 월별 매출 데이터 예시 (실제 DB 쿼리 결과로 대체 가능)
        // 실무에서는 @Query("SELECT SUM(amount) FROM Payment GROUP BY FUNCTION('MONTH', createDate)") 형태 사용
        List<String> months = Arrays.asList("9월", "10월", "11월", "12월", "1월", "2월");
        List<Long> salesData = Arrays.asList(500000L, 800000L, 450000L, 1200000L, 900000L, 1500000L);

        model.addAttribute("months", months);
        model.addAttribute("salesData", salesData);

        return "admin/dashboard";
    }

    @GetMapping("/")
    public String adminHome(){
        return "redirect:/admin/dashboard";
    }
    @GetMapping("/members")
    public String memberList(Model model) {
        model.addAttribute("members", memberService.findAllMembers());
        return "admin/memberList";
    }
    @PostMapping("/members/update-role")
    public String updateMemberRole(@RequestParam Long memberId, @RequestParam String role) {
        memberService.updateRole(memberId, role);
        return "redirect:/admin/members"; // 처리 후 목록으로 리다이렉트
    }

    private final NotificationService notificationService;
    @PostMapping("/members/send-notice")
    public ResponseEntity<String> sendToUser(@RequestParam String email, @RequestParam String message) {
        notificationService.sendNotification(email, message);
        return ResponseEntity.ok("알림 전송 완료");
    }
    @GetMapping("/payments")
    public String paymentList(Model model) {
        // 모든 결제 내역을 가져와서 시간 역순(최신순)으로 정렬하여 모델에 담기
        // PaymentRepository에 findAllByOrderByIdDesc() 등이 구현되어 있어야 함
        List<Payment> payments = paymentRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        model.addAttribute("payments", payments);

        return "admin/paymentList";
    }
    @PostMapping("/payments/cancel")
    public String cancelPayment(@RequestParam Long paymentId,
                                @RequestParam String cancelReason,
                                RedirectAttributes redirectAttributes) {
        try {
            paymentService.cancelPayment(paymentId, cancelReason);
            redirectAttributes.addFlashAttribute("message", "결제가 정상적으로 취소되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/payments";
    }
}