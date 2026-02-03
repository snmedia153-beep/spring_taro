package com.example.demo.repository;

import com.example.demo.Entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    /**
     * 이메일로 회원 정보를 조회합니다.
     * @param email 가입 시 사용한 이메일
     * @return Optional 객체로 감싸서 반환 (NPE 방지)
     */
    Optional<Member> findByEmail(String email);

    /**
     * 특정 이메일이 이미 존재하는지 확인합니다.
     * @param email 중복 체크할 이메일
     * @return 존재 여부 (true/false)
     */
    boolean existsByEmail(String email);
}