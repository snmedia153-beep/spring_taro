package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 결제 API 테스트를 위해 CSRF 비활성화
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/terms","/privacy","/signup", "/api/auth/signup", "/login", "/index.html", "/css/**", "/js/**","/images/**").permitAll() // 메인페이지 허용
                        .anyRequest().authenticated() // 나머지는 로그인 필요
                ).formLogin(form -> form
                        .loginPage("/login")            // 커스텀 로그인 페이지 설정
                        .loginProcessingUrl("/login")   // POST /login 요청을 시큐리티가 낚아챔
                        .defaultSuccessUrl("/", true)   // 성공 시 메인으로
                        .permitAll()
                )
                .logout(logout -> logout.logoutSuccessUrl("/"));

        return http.build();
    }
}