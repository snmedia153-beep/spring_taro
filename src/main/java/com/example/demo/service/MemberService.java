package com.example.demo.service;

import com.example.demo.Entity.Member;
import com.example.demo.dto.SignupRequestDto;
import com.example.demo.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
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
}