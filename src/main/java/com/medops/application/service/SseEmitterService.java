package com.medops.application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class SseEmitterService {

    // 병원별로 연결된 SseEmitter들을 관리
    private final Map<String, List<SseEmitter>> hospitalEmitters = new ConcurrentHashMap<>();

    /**
     * 새로운 SSE 연결을 등록
     */
    public SseEmitter subscribe(String hospitalId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE); // 무제한 타임아웃
        
        // 병원별 emitter 리스트에 추가
        hospitalEmitters.computeIfAbsent(hospitalId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        
        log.info("새 SSE 연결 등록: hospitalId={}, 현재 연결 수={}", 
                hospitalId, hospitalEmitters.get(hospitalId).size());

        // 연결 완료 시점에서 제거 처리
        emitter.onCompletion(() -> removeEmitter(hospitalId, emitter));
        emitter.onTimeout(() -> removeEmitter(hospitalId, emitter));
        emitter.onError(throwable -> {
            log.error("SSE 연결 오류: hospitalId={}", hospitalId, throwable);
            removeEmitter(hospitalId, emitter);
        });

        // 연결 확인 메시지 전송
        try {
            emitter.send(SseEmitter.event()
                    .name("CONNECTED")
                    .data("SSE 연결이 설정되었습니다."));
        } catch (IOException e) {
            log.error("초기 메시지 전송 실패: hospitalId={}", hospitalId, e);
            removeEmitter(hospitalId, emitter);
        }

        return emitter;
    }

    /**
     * 특정 병원의 모든 관리자에게 메시지 전송
     */
    public void sendToHospital(String hospitalId, String eventType, Object data) {
        List<SseEmitter> emitters = hospitalEmitters.get(hospitalId);
        
        if (emitters == null || emitters.isEmpty()) {
            log.debug("해당 병원에 연결된 관리자 없음: hospitalId={}", hospitalId);
            return;
        }

        log.info("SSE 메시지 전송: hospitalId={}, eventType={}, 대상 수={}", 
                hospitalId, eventType, emitters.size());

        // 모든 emitter에게 메시지 전송 (역순으로 순회하여 안전하게 제거)
        for (int i = emitters.size() - 1; i >= 0; i--) {
            SseEmitter emitter = emitters.get(i);
            try {
                emitter.send(SseEmitter.event()
                        .name(eventType)
                        .data(data));
            } catch (IOException e) {
                log.warn("SSE 메시지 전송 실패, 연결 제거: hospitalId={}", hospitalId, e);
                emitters.remove(i);
            }
        }
    }

    /**
     * Emitter 제거
     */
    private void removeEmitter(String hospitalId, SseEmitter emitter) {
        List<SseEmitter> emitters = hospitalEmitters.get(hospitalId);
        if (emitters != null) {
            emitters.remove(emitter);
            log.info("SSE 연결 제거: hospitalId={}, 남은 연결 수={}", 
                    hospitalId, emitters.size());
            
            // 빈 리스트는 제거
            if (emitters.isEmpty()) {
                hospitalEmitters.remove(hospitalId);
            }
        }
    }

    /**
     * 현재 연결 상태 조회 (모니터링용)
     */
    public Map<String, Integer> getConnectionStatus() {
        Map<String, Integer> status = new ConcurrentHashMap<>();
        hospitalEmitters.forEach((hospitalId, emitters) -> 
                status.put(hospitalId, emitters.size()));
        return status;
    }
}