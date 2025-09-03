package com.medops.application.port.in.usecase;

import com.medops.application.port.out.LoadMedicalRecordEventPort;
import com.medops.application.service.MedicalRecordEventService;
import com.medops.domain.enums.MedicalRecordStatus;
import com.medops.domain.event.MedicalRecordEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicalRecordEventUseCaseTest {

    @Mock private LoadMedicalRecordEventPort loadMedicalRecordEventPort;

    private MedicalRecordEventUseCase medicalRecordEventUseCase;

    private MedicalRecordEvent testEvent;

    @BeforeEach
    void setUp() {
        medicalRecordEventUseCase = new MedicalRecordEventService(loadMedicalRecordEventPort);

        testEvent = MedicalRecordEvent.builder()
            .id("event-1")
            .recordId("record-1")
            .createdAt(Instant.now())
            .eventType("RECORD_CREATED")
            .hospitalId("hospital-1")
            .userId("user-1")
            .version(1)
            .status(MedicalRecordStatus.PENDING)
            .payload(Map.of(
                "doctorId", "doctor-1",
                "note", "감기 진료",
                "startTime", Instant.now().toString(),
                "endTime", Instant.now().plusSeconds(1800).toString()
            ))
            .build();
    }

    @Test
    @DisplayName("진료기록 ID로 이벤트 목록 조회 성공")
    void should_returnEvents_when_getEventsByRecordId() {
        // given
        String recordId = "record-1";
        List<MedicalRecordEvent> expectedEvents = List.of(testEvent);
        
        when(loadMedicalRecordEventPort.loadEventsByRecordId(recordId))
            .thenReturn(expectedEvents);

        // when
        List<MedicalRecordEvent> result = medicalRecordEventUseCase.getEventsByRecordId(recordId);

        // then
        assertEquals(expectedEvents, result);
        assertEquals(1, result.size());
        assertEquals("event-1", result.get(0).getId());
        assertEquals("record-1", result.get(0).getRecordId());
        assertEquals("RECORD_CREATED", result.get(0).getEventType());
        verify(loadMedicalRecordEventPort).loadEventsByRecordId(recordId);
    }

    @Test
    @DisplayName("존재하지 않는 진료기록 ID로 이벤트 조회시 빈 목록 반환")
    void should_returnEmptyList_when_recordNotExists() {
        // given
        String nonExistentRecordId = "nonexistent-record";
        
        when(loadMedicalRecordEventPort.loadEventsByRecordId(nonExistentRecordId))
            .thenReturn(List.of());

        // when
        List<MedicalRecordEvent> result = medicalRecordEventUseCase.getEventsByRecordId(nonExistentRecordId);

        // then
        assertTrue(result.isEmpty());
        verify(loadMedicalRecordEventPort).loadEventsByRecordId(nonExistentRecordId);
    }

    @Test
    @DisplayName("여러 이벤트가 있는 진료기록 조회")
    void should_returnMultipleEvents_when_recordHasMultipleEvents() {
        // given
        String recordId = "record-1";
        
        MedicalRecordEvent event2 = MedicalRecordEvent.builder()
            .id("event-2")
            .recordId(recordId)
            .createdAt(Instant.now().plusSeconds(60))
            .eventType("STATUS_UPDATED")
            .hospitalId("hospital-1")
            .userId("user-1")
            .version(2)
            .status(MedicalRecordStatus.RESERVED)
            .payload(Map.of(
                "previousStatus", "PENDING",
                "newStatus", "RESERVED",
                "updatedBy", "admin-1"
            ))
            .build();
            
        MedicalRecordEvent event3 = MedicalRecordEvent.builder()
            .id("event-3")
            .recordId(recordId)
            .createdAt(Instant.now().plusSeconds(120))
            .eventType("RECORD_COMPLETED")
            .hospitalId("hospital-1")
            .userId("user-1")
            .version(3)
            .status(MedicalRecordStatus.COMPLETED)
            .payload(Map.of(
                "diagnosis", "감기",
                "prescription", "해열제",
                "completedBy", "doctor-1"
            ))
            .build();
        
        List<MedicalRecordEvent> multipleEvents = List.of(testEvent, event2, event3);
        when(loadMedicalRecordEventPort.loadEventsByRecordId(recordId))
            .thenReturn(multipleEvents);

        // when
        List<MedicalRecordEvent> result = medicalRecordEventUseCase.getEventsByRecordId(recordId);

        // then
        assertEquals(3, result.size());
        assertEquals(multipleEvents, result);
        
        // 이벤트 순서 및 내용 검증
        assertEquals("RECORD_CREATED", result.get(0).getEventType());
        assertEquals("STATUS_UPDATED", result.get(1).getEventType());
        assertEquals("RECORD_COMPLETED", result.get(2).getEventType());
        
        assertEquals(MedicalRecordStatus.PENDING, result.get(0).getStatus());
        assertEquals(MedicalRecordStatus.RESERVED, result.get(1).getStatus());
        assertEquals(MedicalRecordStatus.COMPLETED, result.get(2).getStatus());
        
        verify(loadMedicalRecordEventPort).loadEventsByRecordId(recordId);
    }

    @Test
    @DisplayName("이벤트 페이로드 정보 확인")
    void should_returnCorrectPayload_when_getEventsByRecordId() {
        // given
        String recordId = "record-1";
        List<MedicalRecordEvent> expectedEvents = List.of(testEvent);
        
        when(loadMedicalRecordEventPort.loadEventsByRecordId(recordId))
            .thenReturn(expectedEvents);

        // when
        List<MedicalRecordEvent> result = medicalRecordEventUseCase.getEventsByRecordId(recordId);

        // then
        assertEquals(1, result.size());
        MedicalRecordEvent event = result.get(0);
        
        assertNotNull(event.getPayload());
        assertEquals("doctor-1", event.getPayload().get("doctorId"));
        assertEquals("감기 진료", event.getPayload().get("note"));
        assertTrue(event.getPayload().containsKey("startTime"));
        assertTrue(event.getPayload().containsKey("endTime"));
        
        verify(loadMedicalRecordEventPort).loadEventsByRecordId(recordId);
    }

    @Test
    @DisplayName("이벤트 버전 정보 확인")
    void should_returnCorrectVersion_when_getEventsByRecordId() {
        // given
        String recordId = "record-1";
        List<MedicalRecordEvent> expectedEvents = List.of(testEvent);
        
        when(loadMedicalRecordEventPort.loadEventsByRecordId(recordId))
            .thenReturn(expectedEvents);

        // when
        List<MedicalRecordEvent> result = medicalRecordEventUseCase.getEventsByRecordId(recordId);

        // then
        assertEquals(1, result.size());
        MedicalRecordEvent event = result.get(0);
        
        assertEquals(Integer.valueOf(1), event.getVersion());
        assertEquals("hospital-1", event.getHospitalId());
        assertEquals("user-1", event.getUserId());
        assertNotNull(event.getCreatedAt());
        
        verify(loadMedicalRecordEventPort).loadEventsByRecordId(recordId);
    }

    @Test
    @DisplayName("다양한 이벤트 타입 확인")
    void should_handleDifferentEventTypes_when_getEventsByRecordId() {
        // given
        String recordId = "record-1";
        
        MedicalRecordEvent cancelEvent = MedicalRecordEvent.builder()
            .id("event-cancel")
            .recordId(recordId)
            .createdAt(Instant.now())
            .eventType("RECORD_CANCELED")
            .hospitalId("hospital-1")
            .userId("user-1")
            .version(1)
            .status(MedicalRecordStatus.CANCELED)
            .payload(Map.of(
                "cancelReason", "환자 요청",
                "canceledBy", "user-1",
                "canceledAt", Instant.now().toString()
            ))
            .build();
        
        List<MedicalRecordEvent> events = List.of(cancelEvent);
        when(loadMedicalRecordEventPort.loadEventsByRecordId(recordId))
            .thenReturn(events);

        // when
        List<MedicalRecordEvent> result = medicalRecordEventUseCase.getEventsByRecordId(recordId);

        // then
        assertEquals(1, result.size());
        MedicalRecordEvent event = result.get(0);
        
        assertEquals("RECORD_CANCELED", event.getEventType());
        assertEquals(MedicalRecordStatus.CANCELED, event.getStatus());
        assertEquals("환자 요청", event.getPayload().get("cancelReason"));
        
        verify(loadMedicalRecordEventPort).loadEventsByRecordId(recordId);
    }

    @Test
    @DisplayName("빈 페이로드를 가진 이벤트 처리")
    void should_handleEmptyPayload_when_getEventsByRecordId() {
        // given
        String recordId = "record-1";
        
        MedicalRecordEvent eventWithEmptyPayload = MedicalRecordEvent.builder()
            .id("event-empty")
            .recordId(recordId)
            .createdAt(Instant.now())
            .eventType("SIMPLE_EVENT")
            .hospitalId("hospital-1")
            .userId("user-1")
            .version(1)
            .status(MedicalRecordStatus.PENDING)
            .payload(Map.of()) // 빈 페이로드
            .build();
        
        List<MedicalRecordEvent> events = List.of(eventWithEmptyPayload);
        when(loadMedicalRecordEventPort.loadEventsByRecordId(recordId))
            .thenReturn(events);

        // when
        List<MedicalRecordEvent> result = medicalRecordEventUseCase.getEventsByRecordId(recordId);

        // then
        assertEquals(1, result.size());
        MedicalRecordEvent event = result.get(0);
        
        assertNotNull(event.getPayload());
        assertTrue(event.getPayload().isEmpty());
        assertEquals("SIMPLE_EVENT", event.getEventType());
        
        verify(loadMedicalRecordEventPort).loadEventsByRecordId(recordId);
    }
}