package com.medops.application.service;

import com.medops.application.port.in.usecase.NotificationUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class SseEmitterService implements NotificationUseCase {

    // 병원별로 연결된 SseEmitter들을 관리
    private final Map<String, List<SseEmitter>> hospitalEmitters = new ConcurrentHashMap<>();

    public SseEmitterService() {
        // 주기적으로 연결 상태 확인을 위한 스케줄러
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleWithFixedDelay(this::cleanupDeadConnections, 10, 10, TimeUnit.SECONDS);
    }

    /**
     * 새로운 SSE 연결을 등록
     */
    public SseEmitter subscribe(String hospitalId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        
        // 병원별 emitter 리스트에 추가
        hospitalEmitters.computeIfAbsent(hospitalId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        
        log.info("새 SSE 연결 등록: hospitalId={}, 현재 연결 수={}", hospitalId, hospitalEmitters.get(hospitalId).size());

        emitter.onCompletion(() -> {
            log.info("SSE 연결 정상 종료: hospitalId={}", hospitalId);
            removeEmitter(hospitalId, emitter);
        });
        emitter.onTimeout(() -> {
            log.info("SSE 연결 타임아웃: hospitalId={}", hospitalId);
            removeEmitter(hospitalId, emitter);
        });
        emitter.onError(throwable -> {
            log.error("SSE 연결 오류: hospitalId={}", hospitalId, throwable);
            removeEmitter(hospitalId, emitter);
        });

        // 연결 확인 메시지 전송
        try {
            emitter.send(SseEmitter.event().name("CONNECTED").data("SSE 연결이 설정되었습니다."));
        } catch (IOException e) {
            log.warn("초기 메시지 전송 실패: hospitalId={}", hospitalId, e);
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
            return;
        }

        log.info("SSE 메시지 전송: hospitalId={}, eventType={}, 대상 수={}", hospitalId, eventType, emitters.size());

        // 모든 emitter에게 메시지 전송
        List<SseEmitter> failedEmitters = new ArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name(eventType).data(data));
            } catch (Exception e) {
                log.warn("SSE 메시지 전송 실패: hospitalId={}, eventType={}", hospitalId, eventType, e);
                failedEmitters.add(emitter);
            }
        }
        
        // 실패한 emitter들을 안전하게 제거
        for (SseEmitter failedEmitter : failedEmitters) {
            emitters.remove(failedEmitter);
        }
        
        if (!failedEmitters.isEmpty()) {
            log.info("SSE 메시지 전송 완료: hospitalId={}, 실패 수={}, 남은 연결={}", 
                hospitalId, failedEmitters.size(), emitters.size());
        }
    }

    /**
     * Emitter 제거
     */
    private void removeEmitter(String hospitalId, SseEmitter emitter) {
        List<SseEmitter> emitters = hospitalEmitters.get(hospitalId);
        if (emitters != null) {
            emitters.remove(emitter);
            log.info("SSE 연결 제거: hospitalId={}, 남은 연결 수={}", hospitalId, emitters.size());
            
            // 빈 리스트는 제거
            if (emitters.isEmpty()) {
                hospitalEmitters.remove(hospitalId);
            }
        }
    }
    
    /**
     * 죽은 연결들을 정리
     */
    private void cleanupDeadConnections() {
        try {
            for (Map.Entry<String, List<SseEmitter>> entry : hospitalEmitters.entrySet()) {
                String hospitalId = entry.getKey();
                List<SseEmitter> emitters = entry.getValue();
                List<SseEmitter> deadEmitters = new ArrayList<>();
                
                for (SseEmitter emitter : emitters) {
                    if (!isConnectionAlive(emitter)) {
                        deadEmitters.add(emitter);
                    }
                }
                
                // 죽은 연결들 제거
                for (SseEmitter deadEmitter : deadEmitters) {
                    emitters.remove(deadEmitter);
                }
                
                if (!deadEmitters.isEmpty()) {
                    log.info("죽은 SSE 연결 제거: hospitalId={}, 제거된 수={}", hospitalId, deadEmitters.size());
                }
                
                // 빈 리스트는 제거
                if (emitters.isEmpty()) {
                    hospitalEmitters.remove(hospitalId);
                }
            }
        } catch (Exception e) {
            log.error("죽은 SSE 연결 정리 중 오류 발생", e);
        }
    }
    
    /**
     * 연결이 살아있는지 확인 (heartbeat 전송)
     */
    private boolean isConnectionAlive(SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event().name("HEARTBEAT").data("ping"));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}