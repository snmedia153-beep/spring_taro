package com.example.demo.controller;

import com.example.demo.Entity.Member;
import com.example.demo.Entity.Payment;
import com.example.demo.Entity.Reservation;
import com.example.demo.Interface.ReservationRepository;
import com.example.demo.Security.PrincipalDetails;
import com.example.demo.dto.SignupRequestDto;
import com.example.demo.dto.TarotCard;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.service.MemberService;
import com.example.demo.service.PaymentService;
import com.example.demo.service.TarotHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final PaymentRepository paymentRepository; // 예약 정보를 저장할 리포지토리
    private final MemberService memberService;
    private final PaymentService paymentService;
    private final TarotHistoryService tarotHistoryService;

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

        // ✅ 'DONE' 또는 'PAID' 상태인 최신 결제건이 있는지 확인
        String email = principalDetails.getMember().getName();
        boolean canUseTarot = paymentService.checkActiveTicket(email);
        model.addAttribute("canUseTarot", canUseTarot);

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

    @GetMapping("/api/tarot/today")
    @ResponseBody
    public ResponseEntity<TarotCard> drawTodayCardApi(@RequestParam Long paymentId, Principal principal) {
        String email = principal.getName();

        // 결제 정보 가져오기
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

        // 중복 방지 로직 (이미 사용한 티켓인지 확인)
        if ("USED".equals(payment.getTarotStatus())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        List<TarotCard> deck = getMajorArcanaDeck();
        TarotCard randomCard = deck.get(new Random().nextInt(deck.size()));

        Member member = memberService.findByEmail(email);

        // ✅ 저장 시 payment 객체를 함께 넘깁니다.
        tarotHistoryService.saveHistory(member, randomCard, payment);

        // ✅ 결제 상태를 USED로 변경
        paymentService.updateTarotStatus(paymentId, "USED");

        return ResponseEntity.ok(randomCard);
    }
    @GetMapping("/api/tarot/history/{paymentId}")
    @ResponseBody
    public ResponseEntity<TarotCard> getTarotHistory(@PathVariable Long paymentId, Principal principal) {
        // 결제 ID에 매칭되는 타로 기록을 찾아 반환합니다.
        // tarotHistoryService에서 paymentId로 저장된 카드를 찾는 로직이 필요합니다.
        TarotCard historyCard = tarotHistoryService.getCardByPaymentId(paymentId);

        if (historyCard == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(historyCard);
    }
    private List<TarotCard> getMajorArcanaDeck() {
        List<TarotCard> deck = new ArrayList<>();
        // 0. 광대 (The Fool)
        deck.add(new TarotCard(0, "The Fool", "/images/cards/fool.png",
                "새로운 여정의 시작을 의미합니다. 지금은 두려움 없이 발을 내디뎌야 할 때입니다. 순수한 마음이 당신을 예상치 못한 행운으로 안내할 것입니다."));

        // 1. 마법사 (The Magician)
        deck.add(new TarotCard(1, "The Magician", "/images/cards/magician.png",
                "당신은 이미 모든 도구를 갖추고 있습니다. 창의력과 의지력을 발휘하여 무에서 유를 창조하세요. 생각한 것을 현실로 바꿀 수 있는 강력한 시기입니다."));

        // 2. 고위 여사제 (The High Priestess)
        deck.add(new TarotCard(2, "The High Priestess", "/images/cards/priestess.png",
                "내면의 목소리에 귀를 기울이세요. 겉으로 드러나지 않은 비밀이나 지혜가 곧 밝혀질 것입니다. 직관을 믿고 정적인 시간을 갖는 것이 좋습니다."));

        // 4. 황제 (The Emperor)
        deck.add(new TarotCard(4, "The Emperor", "/images/cards/emperor.png",
                "질서와 권위, 그리고 안정감을 상징합니다. 확고한 리더십이 필요한 때이며, 체계적인 계획을 통해 상황을 통제하고 목표를 달성하게 될 것입니다."));
        /*
        // 15. 악마 (The Devil)
        deck.add(new TarotCard(15, "The Devil", "/images/cards/devil.png",
            "무언가에 얽매여 있지는 않나요? 집착이나 유혹에서 벗어나야 함을 경고합니다. 스스로를 가둔 사슬이 무엇인지 돌아보고 자유를 찾기 위한 결단이 필요합니다."));

        // 16. 탑 (The Tower)
        deck.add(new TarotCard(16, "The Tower", "/images/cards/tower.png",
            "갑작스러운 변화나 충격이 찾아올 수 있지만, 이는 낡은 것을 허물고 새로 시작하기 위한 과정입니다. 무너진 자리 위에서 더 단단한 기초를 쌓게 될 것입니다."));

        // 17. 별 (The Star)
        deck.add(new TarotCard(17, "The Star", "/images/cards/star.png",
            "어둠 속에서도 희망의 빛이 보입니다. 영감과 치유의 에너지가 흐르는 시기입니다. 당신의 꿈을 믿고 나아간다면 마침내 평온과 보상을 얻게 됩니다."));

        // 21. 세계 (The World)
        deck.add(new TarotCard(21, "The World", "/images/cards/world.png",
            "하나의 주기가 완성되었습니다. 성공적인 마무리와 통합을 의미하며, 당신의 노력이 결실을 맺어 완벽한 조화를 이루게 될 최고의 카드입니다."));
        */
        // ... 21번까지 추가
        return deck;
    }
}