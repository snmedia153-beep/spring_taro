package com.example.demo.config;
import com.example.demo.Entity.Member;
import com.example.demo.Security.PrincipalDetails;
import com.example.demo.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PrincipalDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    // 시큐리티 로그인 폼에서 /login 주소로 로그인을 시도하면 이 함수가 자동 실행됩니다.
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // 1. DB에서 이메일로 회원 조회
        Member memberEntity = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("해당 이메일을 가진 운명을 찾을 수 없습니다: " + email));

        // 2. 시큐리티가 이해할 수 있는 형태(UserDetails)로 반환
        // PrincipalDetails는 이전에 만든 사용자 정보 객체입니다.
        return new PrincipalDetails(memberEntity);
    }
}