package com.example.demo.controller;

import com.example.demo.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequiredArgsConstructor // ✅ 이 어노테이션이 있어야 final 필드를 생성자로 주입합니다.
public class MemberController {
    private final MemberService memberService; // ✅ 반드시 final이 붙어 있어야 합니다.
    @PostMapping("/member/email-check")
    public @ResponseBody String emailCheck(@RequestParam("memberEmail") String memberEmail) {
        System.out.println(memberEmail);
        String checkResult = memberService.emailCheck(memberEmail);
        return checkResult;
    }
    @Controller
    @RequestMapping("/api/member")
    @RequiredArgsConstructor
    public class MemberApiController {

        private final MemberService memberService;

        @PostMapping("/update-password")
        public String updatePassword(@RequestParam String currentPassword,
                                     @RequestParam String newPassword,
                                     @RequestParam String confirmPassword,
                                     Principal principal,
                                     RedirectAttributes redirectAttributes) {
            try {
                // 현재 로그인한 사용자의 이메일 가져오기
                String email = principal.getName();

                memberService.updatePassword(email, currentPassword, newPassword, confirmPassword);

                redirectAttributes.addFlashAttribute("message", "비밀번호가 성공적으로 변경되었습니다.");
                return "redirect:/mypage"; // 성공 시 마이페이지로 리다이렉트

            } catch (IllegalArgumentException e) {
                redirectAttributes.addFlashAttribute("error", e.getMessage());
                return "redirect:/mypage"; // 실패 시 에러 메시지와 함께 리다이렉트
            }
        }
    }
}