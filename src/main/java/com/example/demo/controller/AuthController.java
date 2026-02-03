package com.example.demo.controller;

import com.example.demo.Entity.Payment;
import com.example.demo.Entity.Reservation;
import com.example.demo.Interface.ReservationRepository;
import com.example.demo.Security.PrincipalDetails;
import com.example.demo.dto.SignupRequestDto;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final PaymentRepository paymentRepository; // 예약 정보를 저장할 리포지토리
    private final MemberService memberService;

    // 로그인 페이지 이동
    @GetMapping("/login")
    public String loginPage() {
        return "login"; // login.html 반환
    }

    // 회원가입 페이지 이동
    @GetMapping("/signup")
    public String signupPage() {
        return "signup"; // signup.html 반환
    }
    @GetMapping("/mypage")
    public String myPage(@AuthenticationPrincipal PrincipalDetails principalDetails, Model model) {
        Long memberId = principalDetails.getMember().getId();

        // 사용자의 결제 내역 가져오기 (ReservationRepository 활용)
        List<Payment> paymentList = paymentRepository.findByMemberId(memberId);

        model.addAttribute("paymentList", paymentList);
        return "mypage";
    }
    // 회원가입 로직 처리
    @PostMapping("/api/auth/signup")
    public String signup(@ModelAttribute SignupRequestDto requestDto) {
        try {
            memberService.join(requestDto);
            return "redirect:/login?success=true"; // 가입 성공 시 로그인 페이지로
        } catch (IllegalStateException e) {
            return "redirect:/signup?error=" + e.getMessage(); // 실패 시 에러 메시지 포함
        }
    }
}