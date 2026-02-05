package com.example.demo.Security;

import com.example.demo.Entity.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

// Spring Security가 로그인 완료된 사용자를 저장할 통(Container)입니다.
public class PrincipalDetails implements UserDetails {
    private Member member;

    public PrincipalDetails(Member member) {
        this.member = member;
    }

    public Member getMember() { return member; }
    // ✅ 타임리프에서 memberId로 접근할 수 있도록 Getter 추가
    public Long getMemberId() {
        return member.getId();
    }

    // ✅ 추가로 유저의 실제 이름 등이 필요하다면 미리 만들어두는 것이 좋습니다.
    public String getRealName() {
        return member.getName();
    }
    /*
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(member.getRole()));
    }*/
    // 해당 유저의 권한을 리턴하는 곳! (ROLE_USER, ROLE_ADMIN 등)
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collect = new ArrayList<>();
        collect.add(() -> member.getRole());
        return collect;
    }
    @Override
    public String getPassword() { return member.getPassword(); }

    @Override
    public String getUsername() { return member.getEmail(); }

    // 나머지 메서드들은 true로 설정 (계정 잠금, 만료 여부 등)
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() {
        // 아까 만든 활동 정지(ROLE_BANNED) 유저는 여기서 false를 리턴하게 할 수도 있습니다.
        return !"ROLE_BANNED".equals(member.getRole());
    }
}