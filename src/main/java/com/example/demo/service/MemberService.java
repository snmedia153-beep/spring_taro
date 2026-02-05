package com.example.demo.service;

import com.example.demo.Entity.Member;
import com.example.demo.dto.SignupRequestDto;
import com.example.demo.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor// final 필드에 대한 생성자를 자동으로 만들어줍니다.
public class MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public void join(SignupRequestDto requestDto) {
        // 1. 이메일 중복 체크
        memberRepository.findByEmail(requestDto.getEmail())
                .ifPresent(m -> {
                    throw new IllegalStateException("이미 존재하는 운명(이메일)입니다.");
                });

        // 2. 비밀번호 암호화 및 저장
        Member member = new Member();
        member.setName(requestDto.getName());
        member.setEmail(requestDto.getEmail());
        member.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        member.setRole("ROLE_USER");

        memberRepository.save(member);
    }
    public String emailCheck(String email) {
        boolean exists = memberRepository.existsByEmail(email);
        if (exists) {
            return "invalid"; // 이미 존재함
        } else {
            return "ok";      // 사용 가능
        }
    }
    public Member findByEmail(String email) {
        Optional<Member> member = memberRepository.findByEmail(email);
        //Member member = memberService.findByEmail(email);
        return member.orElse(null);
    }
    /**
     * ✅ 관리자 페이지용: 전체 회원 목록 조회
     */
    public List<Member> findAllMembers() {
        return memberRepository.findAll();
    }
    @Transactional
    public void updateRole(Long memberId, String newRole) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 운명이 존재하지 않습니다."));

        // ROLE_ADMIN, ROLE_USER, ROLE_BANNED(정지) 등으로 설정 가능
        member.setRole(newRole);
    }
    @Transactional
    public void updatePassword(String email, String currentPassword, String newPassword, String confirmPassword) {
        // 1. 회원 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 2. 현재 비밀번호 일치 확인 (BCrypt 매칭 사용)
        if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 3. 새 비밀번호와 확인용 비밀번호 일치 확인
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
        }

        // 4. 새 비밀번호 암호화 후 저장
        member.setPassword(passwordEncoder.encode(newPassword));
        // @Transactional에 의해 자동 업데이트(Dirty Checking)
    }
}