package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NotificationService {
    // 사용자 ID별로 연결된 SseEmitter를 저장 (동시성 고려)
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

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
        if (emitters.containsKey(email)) {
            try {
                emitters.get(email).send(SseEmitter.event().name("notification").data(message));
            } catch (IOException e) {
                emitters.remove(email);
            }
        }
    }
}
