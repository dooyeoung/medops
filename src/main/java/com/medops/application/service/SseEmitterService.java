package com.medops.application.service;

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
public class SseEmitterService {

    // 병원별로 연결된 SseEmitter들을 관리
    private final Map<String, List<SseEmitter>> hospitalEmitters = new ConcurrentHashMap<>();
    
    // 주기적으로 연결 상태 확인을 위한 스케줄러
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    public SseEmitterService() {
        System.out.println("SseEmitterService 초기화됨");
        // 30초마다 죽은 연결 정리 (디버깅을 위해 10초로 단축)
        scheduler.scheduleWithFixedDelay(() -> {
            System.out.println("스케줄된 cleanupDeadConnections 실행");
            cleanupDeadConnections();
        }, 10, 10, TimeUnit.SECONDS);
        System.out.println("SSE 연결 정리 스케줄러 시작됨 (10초 간격)");
    }

    /**
     * 새로운 SSE 연결을 등록
     */
    public SseEmitter subscribe(String hospitalId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE); // 무제한 타임아웃
        String emitterId = "emitter-" + System.currentTimeMillis() + "-" + System.identityHashCode(emitter);
        
        // 병원별 emitter 리스트에 추가
        hospitalEmitters.computeIfAbsent(hospitalId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        
        log.info("새 SSE 연결 등록: emitterId={}, hospitalId={}, 현재 연결 수={}", 
                emitterId, hospitalId, hospitalEmitters.get(hospitalId).size());

        // 연결 완료 시점에서 제거 처리
        emitter.onCompletion(() -> {
            log.info("SSE 연결 완료됨: emitterId={}, hospitalId={}", emitterId, hospitalId);
            removeEmitter(hospitalId, emitter);
        });
        emitter.onTimeout(() -> {
            log.info("SSE 연결 타임아웃: emitterId={}, hospitalId={}", emitterId, hospitalId);
            removeEmitter(hospitalId, emitter);
        });
        emitter.onError(throwable -> {
            if (throwable.getClass().getSimpleName().contains("AsyncRequestNotUsableException")) {
                log.debug("SSE 클라이언트 연결 끊김: emitterId={}, hospitalId={}, error={}", 
                        emitterId, hospitalId, throwable.getMessage());
            } else {
                log.error("SSE 연결 오류: emitterId={}, hospitalId={}", emitterId, hospitalId, throwable);
            }
            removeEmitter(hospitalId, emitter);
        });

        // 연결 확인 메시지 전송
        try {
            emitter.send(SseEmitter.event()
                    .name("CONNECTED")
                    .data("SSE 연결이 설정되었습니다."));
        } catch (IOException e) {
            if (e.getMessage() != null && 
                (e.getMessage().contains("Broken pipe") || 
                 e.getMessage().contains("Connection reset"))) {
                log.debug("SSE 초기 연결 중 클라이언트 연결 끊김: hospitalId={}", hospitalId);
            } else {
                log.error("초기 메시지 전송 실패: hospitalId={}", hospitalId, e);
            }
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

        log.info("SSE 메시지 전송 시작: hospitalId={}, eventType={}, 대상 수={}", 
                hospitalId, eventType, emitters.size());

        // 모든 emitter에게 메시지 전송 (Iterator 사용으로 안전한 제거)
        List<SseEmitter> failedEmitters = new ArrayList<>();
        int successCount = 0;
        
        for (int i = 0; i < emitters.size(); i++) {
            SseEmitter emitter = emitters.get(i);
            String emitterId = "emitter-" + System.identityHashCode(emitter);
            
            try {
                log.debug("SSE 메시지 전송 중: emitterId={}, hospitalId={}, eventType={}", 
                        emitterId, hospitalId, eventType);
                        
                emitter.send(SseEmitter.event()
                        .name(eventType)
                        .data(data));
                        
                successCount++;
                log.debug("SSE 메시지 전송 성공: emitterId={}", emitterId);
                
            } catch (Exception e) {
                log.warn("SSE 메시지 전송 실패: emitterId={}, hospitalId={}, eventType={}, error={}", 
                        emitterId, hospitalId, eventType, e.getClass().getSimpleName() + ": " + e.getMessage());
                failedEmitters.add(emitter);
            }
        }
        
        // 실패한 emitter들을 안전하게 제거
        for (SseEmitter failedEmitter : failedEmitters) {
            emitters.remove(failedEmitter);
        }
        
        log.info("SSE 메시지 전송 완료: hospitalId={}, eventType={}, 성공={}, 실패={}, 남은 연결={}", 
                hospitalId, eventType, successCount, failedEmitters.size(), emitters.size());
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
    
    /**
     * 죽은 연결들을 정리
     */
    private void cleanupDeadConnections() {
        try {
            System.out.println("죽은 SSE 연결 정리 시작 - 현재 병원 수: "+ hospitalEmitters.size());
            int totalCleaned = 0;
            
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
                    totalCleaned++;
                }
                
                if (!deadEmitters.isEmpty()) {
                    log.info("죽은 SSE 연결 제거: hospitalId={}, 제거된 수={}, 남은 수={}", 
                            hospitalId, deadEmitters.size(), emitters.size());
                }
                
                // 빈 리스트는 제거
                if (emitters.isEmpty()) {
                    hospitalEmitters.remove(hospitalId);
                }
            }
            
            log.info("전체 죽은 SSE 연결 정리 완료: 제거된 총 수={}, 현재 총 연결 수={}", 
                    totalCleaned, hospitalEmitters.values().stream().mapToInt(List::size).sum());
            
        } catch (Exception e) {
            log.error("죽은 SSE 연결 정리 중 오류 발생", e);
        }
    }
    
    /**
     * 연결이 살아있는지 확인 (heartbeat 전송)
     */
    private boolean isConnectionAlive(SseEmitter emitter) {
        try {
            // 간단한 heartbeat 메시지 전송으로 연결 상태 확인
            emitter.send(SseEmitter.event()
                    .name("HEARTBEAT")
                    .data("ping"));
            return true;
        } catch (Exception e) {
            // 전송 실패하면 죽은 연결로 간주
            log.debug("SSE 연결 상태 확인 실패 (죽은 연결): {}", e.getMessage());
            return false;
        }
    }
}