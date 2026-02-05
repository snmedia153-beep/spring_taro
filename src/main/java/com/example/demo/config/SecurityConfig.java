package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    private final PrincipalDetailsService principalDetailsService;
    // SecurityConfig.java 내부
    private final LoginFailureHandler loginFailureHandler;

    // 생성자 주입
    public SecurityConfig(LoginFailureHandler loginFailureHandler,
                          PrincipalDetailsService principalDetailsService) {
        this.loginFailureHandler = loginFailureHandler;
        this.principalDetailsService = principalDetailsService;
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 결제 API 테스트를 위해 CSRF 비활성화
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/**").hasRole("ADMIN")// ✅ 관리자 전용 경로 추가
                        .requestMatchers("/","/favicon.ico", "/terms","/privacy","/signup", "/api/auth/signup", "/login", "/index.html", "/member/email-check","/css/**", "/js/**","/images/**").permitAll() // 메인페이지 허용
                        .anyRequest().authenticated() // 나머지는 로그인 필요
                ).formLogin(form -> form
                        .loginPage("/login")            // 커스텀 로그인 페이지 설정
                        .loginProcessingUrl("/login")   // POST /login 요청을 시큐리티가 낚아챔
                        .defaultSuccessUrl("/", true)   // 성공 시 메인으로
                        .failureHandler(loginFailureHandler) // ✅ 실패 핸들러 등록
                        .permitAll()
                )
                .logout(logout -> logout.logoutSuccessUrl("/"))
                .userDetailsService(principalDetailsService);

        return http.build();
    }
    // 또는 더 확실한 방법 (WebSecurityCustomizer 사용)
    /*
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        // 보안 필터 자체를 아예 통과하지 않도록 설정
        return (web) -> web.ignoring().requestMatchers("/favicon.ico", "/static/**", "/error");
    }
    */
}