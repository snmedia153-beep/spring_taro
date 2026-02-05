package com.example.demo.config;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;

@Component
public class LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        String errorMessage = "이메일 또는 비밀번호가 일치하지 않습니다.";

        // 한글 깨짐 방지를 위한 인코딩
        errorMessage = URLEncoder.encode(errorMessage, "UTF-8");

        // 로그인 페이지로 에러 파라미터를 들고 이동
        setDefaultFailureUrl("/login?error=true&exception=" + errorMessage);
        // 콘솔에서 실제 에러 원인 확인 (비번 불일치인지, 유저가 없는건지 등)
        System.out.println("로그인 실패 이유: " + exception.getMessage());
        super.onAuthenticationFailure(request, response, exception);
    }
}