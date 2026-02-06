package com.example.demo.controller;

import com.example.demo.Security.PrincipalDetails;
import com.example.demo.service.NotificationService;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        // principalDetails에서 실제 Member 객체를 꺼내서 ID를 전달합니다.
        return notificationService.subscribe(principalDetails.getMember().getEmail());
    }
    @GetMapping("/unread")
    public ResponseEntity<List<Map<String, Object>>> getUnreadNotifications(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 세션에서 현재 로그인한 사용자의 이메일을 가져옴 (변조 불가능)
        String loginEmail = principal.getName();

        // 서비스에서 Supabase 조회 로직 호출
        List<Map<String, Object>> unreadList = notificationService.getUnreadFromSupabase(loginEmail);

        return ResponseEntity.ok(unreadList);
    }

    @PostMapping("/read/{id}")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id, Principal principal) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        // 본인의 알림인지 한 번 더 검증하는 로직을 서비스에 포함 권장
        notificationService.markAsReadInSupabase(id);
        return ResponseEntity.ok().build();
    }
}