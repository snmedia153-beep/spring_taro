package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NotificationService {
    // 사용자 ID별로 연결된 SseEmitter를 저장 (동시성 고려)
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(60L * 1000 * 60); // 1시간 타임아웃
        emitters.put(userId, emitter);

        // 연결 종료 시 처리
        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));

        // 초기 연결 시 더미 데이터 전송 (503 에러 방지)
        try {
            emitter.send(SseEmitter.event().name("connect").data("Connected!"));
        } catch (IOException e) {
            emitters.remove(userId);
        }
        return emitter;
    }

    public void send(Long userId, String message) {
        if (emitters.containsKey(userId)) {
            try {
                emitters.get(userId).send(SseEmitter.event().name("notification").data(message));
            } catch (IOException e) {
                emitters.remove(userId);
            }
        }
    }
}
