package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NotificationService {
    // 사용자 ID별로 연결된 SseEmitter를 저장 (동시성 고려)
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final RestTemplate restTemplate;

    @Value("${supabase.url}") // application.properties에 설정
    private String supabaseUrl;

    @Value("${supabase.key}") // application.properties에 설정
    private String supabaseKey;

    public NotificationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    public SseEmitter subscribe(String email) { // ✅ 파라미터 타입 변경
        SseEmitter emitter = new SseEmitter(60L * 1000 * 60);
        emitters.put(email, emitter); // ✅ String 키 사용

        emitter.onCompletion(() -> emitters.remove(email));
        emitter.onTimeout(() -> emitters.remove(email));

        try {
            emitter.send(SseEmitter.event().name("connect").data("Connected!"));
        } catch (IOException e) {
            emitters.remove(email);
        }
        return emitter;
    }
    public void sendNotification(String email, String message) {
        // 1. Supabase DB에 알림 저장 (비접속자 대비)
        saveNotificationToSupabase(email, message);

        if (emitters.containsKey(email)) {
            try {
                emitters.get(email).send(SseEmitter.event().name("notification").data(message));
            } catch (IOException e) {
                emitters.remove(email);
            }
        }
    }
    private void saveNotificationToSupabase(String email, String message) {
        String url = supabaseUrl + "/rest/v1/notifications";

        // 1. 헤더 설정 (Supabase API는 API Key와 Bearer Token이 필요함)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", supabaseKey);
        headers.set("Authorization", "Bearer " + supabaseKey);

        // 2. 바디 데이터 설정
        Map<String, Object> body = new HashMap<>();
        body.put("user_email", email);
        body.put("message", message);
        body.put("is_read", false); // 초기값 미확인

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        try {
            // 3. POST 요청 보냄
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            if (response.getStatusCode() == HttpStatus.CREATED || response.getStatusCode() == HttpStatus.OK) {
                System.out.println("Supabase 알림 저장 성공");
            }

            if (emitters.containsKey(email)) {
                try {
                    emitters.get(email).send(SseEmitter.event().name("notification").data(message));
                } catch (IOException e) {
                    emitters.remove(email);
                }
            }
        } catch (Exception e) {
            System.err.println("Supabase 저장 실패: " + e.getMessage());
        }
    }

    public List<Map<String, Object>> getUnreadFromSupabase(String email) {
        // URL에 필터 조건 추가 (user_email = 로그인유저, is_read = false)
        String url = supabaseUrl + "/rest/v1/notifications?user_email=eq." + email + "&is_read=eq.false";

        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", supabaseKey);
        headers.set("Authorization", "Bearer " + supabaseKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);
            return response.getBody();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public void markAsReadInSupabase(Long id) {
        String url = supabaseUrl + "/rest/v1/notifications?id=eq." + id;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", supabaseKey);
        headers.set("Authorization", "Bearer " + supabaseKey);

        Map<String, Object> body = new HashMap<>();
        body.put("is_read", true);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        restTemplate.exchange(url, HttpMethod.PATCH, entity, String.class);
    }
}
