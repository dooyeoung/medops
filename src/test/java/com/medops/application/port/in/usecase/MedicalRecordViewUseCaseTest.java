package com.medops.application.port.in.usecase;

import com.medops.adapter.out.persistence.mongodb.document.MedicalRecordViewDocument;
import com.medops.application.port.out.LoadMedicalRecordViewPort;
import com.medops.application.service.MedicalRecordViewService;
import com.medops.common.exception.NotFoundResource;
import com.medops.domain.enums.MedicalRecordStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicalRecordViewUseCaseTest {

    @Mock private LoadMedicalRecordViewPort loadMedicalRecordViewPort;

    private MedicalRecordViewUseCase medicalRecordViewUseCase;

    private MedicalRecordViewDocument testMedicalRecord;

    @BeforeEach
    void setUp() {
        medicalRecordViewUseCase = new MedicalRecordViewService(loadMedicalRecordViewPort);

        testMedicalRecord = new MedicalRecordViewDocument(
            "record-1",
            "user-1",
            "김환자",
            "hospital-1",
            "테스트병원",
            "doctor-1",
            "김의사",
            "감기 진료",
            MedicalRecordStatus.COMPLETED,
            Instant.now().minusSeconds(3600),
            Instant.now(),
            "product-1",
            "일반상담",
            "환자 메모"
        );
    }

    @Test
    @DisplayName("병원별 기간내 진료기록 조회 성공")
    void should_returnMedicalRecords_when_getMedicalRecordsByHospital() {
        // given
        String hospitalId = "hospital-1";
        Instant startTime = Instant.now().minusSeconds(7200);
        Instant endTime = Instant.now();
        List<MedicalRecordViewDocument> expectedRecords = List.of(testMedicalRecord);
        
        when(loadMedicalRecordViewPort.loadMedicalRecordsByHospitalInRange(hospitalId, startTime, endTime))
            .thenReturn(expectedRecords);

        // when
        List<MedicalRecordViewDocument> result = medicalRecordViewUseCase
            .getMedicalRecordsByHospital(hospitalId, startTime, endTime);

        // then
        assertEquals(expectedRecords, result);
        assertEquals(1, result.size());
        assertEquals("record-1", result.get(0).getId());
        assertEquals("테스트병원", result.get(0).getHospitalName());
        verify(loadMedicalRecordViewPort).loadMedicalRecordsByHospitalInRange(hospitalId, startTime, endTime);
    }

    @Test
    @DisplayName("병원별 기간내 진료기록이 없는 경우 빈 목록 반환")
    void should_returnEmptyList_when_noMedicalRecordsInHospitalRange() {
        // given
        String hospitalId = "hospital-1";
        Instant startTime = Instant.now().minusSeconds(7200);
        Instant endTime = Instant.now();
        
        when(loadMedicalRecordViewPort.loadMedicalRecordsByHospitalInRange(hospitalId, startTime, endTime))
            .thenReturn(List.of());

        // when
        List<MedicalRecordViewDocument> result = medicalRecordViewUseCase
            .getMedicalRecordsByHospital(hospitalId, startTime, endTime);

        // then
        assertTrue(result.isEmpty());
        verify(loadMedicalRecordViewPort).loadMedicalRecordsByHospitalInRange(hospitalId, startTime, endTime);
    }

    @Test
    @DisplayName("사용자별 진료기록 조회 성공")
    void should_returnMedicalRecords_when_getMedicalRecordsByUser() {
        // given
        String userId = "user-1";
        List<MedicalRecordViewDocument> expectedRecords = List.of(testMedicalRecord);
        
        when(loadMedicalRecordViewPort.loadMedicalRecordsByUserId(userId))
            .thenReturn(expectedRecords);

        // when
        List<MedicalRecordViewDocument> result = medicalRecordViewUseCase.getMedicalRecordsByUser(userId);

        // then
        assertEquals(expectedRecords, result);
        assertEquals(1, result.size());
        assertEquals("김환자", result.get(0).getUserName());
        verify(loadMedicalRecordViewPort).loadMedicalRecordsByUserId(userId);
    }

    @Test
    @DisplayName("사용자별 진료기록이 없는 경우 빈 목록 반환")
    void should_returnEmptyList_when_noMedicalRecordsForUser() {
        // given
        String userId = "user-nonexistent";
        
        when(loadMedicalRecordViewPort.loadMedicalRecordsByUserId(userId))
            .thenReturn(List.of());

        // when
        List<MedicalRecordViewDocument> result = medicalRecordViewUseCase.getMedicalRecordsByUser(userId);

        // then
        assertTrue(result.isEmpty());
        verify(loadMedicalRecordViewPort).loadMedicalRecordsByUserId(userId);
    }

    @Test
    @DisplayName("사용자 및 병원별 진료기록 조회 성공")
    void should_returnMedicalRecords_when_getMedicalRecordsByUserAndHospital() {
        // given
        String userId = "user-1";
        String hospitalId = "hospital-1";
        List<MedicalRecordViewDocument> expectedRecords = List.of(testMedicalRecord);
        
        when(loadMedicalRecordViewPort.loadMedicalRecordsByUserIdAndByHospitalId(userId, hospitalId))
            .thenReturn(expectedRecords);

        // when
        List<MedicalRecordViewDocument> result = medicalRecordViewUseCase
            .getMedicalRecordsByUserAndHospital(userId, hospitalId);

        // then
        assertEquals(expectedRecords, result);
        assertEquals(1, result.size());
        assertEquals("user-1", result.get(0).getUserId());
        assertEquals("hospital-1", result.get(0).getHospitalId());
        verify(loadMedicalRecordViewPort).loadMedicalRecordsByUserIdAndByHospitalId(userId, hospitalId);
    }

    @Test
    @DisplayName("사용자 및 병원별 진료기록이 없는 경우 빈 목록 반환")
    void should_returnEmptyList_when_noMedicalRecordsForUserAndHospital() {
        // given
        String userId = "user-1";
        String hospitalId = "hospital-nonexistent";
        
        when(loadMedicalRecordViewPort.loadMedicalRecordsByUserIdAndByHospitalId(userId, hospitalId))
            .thenReturn(List.of());

        // when
        List<MedicalRecordViewDocument> result = medicalRecordViewUseCase
            .getMedicalRecordsByUserAndHospital(userId, hospitalId);

        // then
        assertTrue(result.isEmpty());
        verify(loadMedicalRecordViewPort).loadMedicalRecordsByUserIdAndByHospitalId(userId, hospitalId);
    }

    @Test
    @DisplayName("진료기록 ID로 단일 진료기록 조회 성공")
    void should_returnMedicalRecord_when_getMedicalRecord() {
        // given
        String recordId = "record-1";
        
        when(loadMedicalRecordViewPort.loadMedicalRecordById(recordId))
            .thenReturn(Optional.of(testMedicalRecord));

        // when
        MedicalRecordViewDocument result = medicalRecordViewUseCase.getMedicalRecord(recordId);

        // then
        assertEquals(testMedicalRecord, result);
        assertEquals("record-1", result.getId());
        assertEquals("감기 진료", result.getNote());
        assertEquals(MedicalRecordStatus.COMPLETED, result.getStatus());
        verify(loadMedicalRecordViewPort).loadMedicalRecordById(recordId);
    }

    @Test
    @DisplayName("존재하지 않는 진료기록 ID로 조회시 NotFoundResource 예외 발생")
    void should_throwNotFoundResource_when_medicalRecordNotExists() {
        // given
        String nonExistentRecordId = "nonexistent-record";
        
        when(loadMedicalRecordViewPort.loadMedicalRecordById(nonExistentRecordId))
            .thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundResource.class, 
            () -> medicalRecordViewUseCase.getMedicalRecord(nonExistentRecordId));
        verify(loadMedicalRecordViewPort).loadMedicalRecordById(nonExistentRecordId);
    }

    @Test
    @DisplayName("여러 진료기록 조회 - 사용자별")
    void should_returnMultipleMedicalRecords_when_userHasMultipleRecords() {
        // given
        String userId = "user-1";
        MedicalRecordViewDocument record2 = new MedicalRecordViewDocument(
            "record-2",
            "user-1",
            "김환자",
            "hospital-2",
            "다른병원",
            "doctor-2",
            "이의사",
            "건강검진",
            MedicalRecordStatus.RESERVED,
            Instant.now().minusSeconds(1800),
            Instant.now().plusSeconds(1800),
            "product-2",
            "정기검진",
            "검진 메모"
        );
        
        List<MedicalRecordViewDocument> multipleRecords = List.of(testMedicalRecord, record2);
        when(loadMedicalRecordViewPort.loadMedicalRecordsByUserId(userId))
            .thenReturn(multipleRecords);

        // when
        List<MedicalRecordViewDocument> result = medicalRecordViewUseCase.getMedicalRecordsByUser(userId);

        // then
        assertEquals(2, result.size());
        assertEquals(multipleRecords, result);
        assertTrue(result.stream().allMatch(record -> record.getUserId().equals(userId)));
        verify(loadMedicalRecordViewPort).loadMedicalRecordsByUserId(userId);
    }

    @Test
    @DisplayName("진료기록 상태별 확인")
    void should_returnCorrectStatus_when_getMedicalRecord() {
        // given
        MedicalRecordViewDocument scheduledRecord = new MedicalRecordViewDocument(
            "record-scheduled",
            "user-1",
            "김환자",
            "hospital-1",
            "테스트병원",
            "doctor-1",
            "김의사",
            "예약된 진료",
            MedicalRecordStatus.RESERVED,
            Instant.now().plusSeconds(3600),
            Instant.now().plusSeconds(7200),
            "product-1",
            "일반상담",
            "예약 메모"
        );
        
        when(loadMedicalRecordViewPort.loadMedicalRecordById("record-scheduled"))
            .thenReturn(Optional.of(scheduledRecord));

        // when
        MedicalRecordViewDocument result = medicalRecordViewUseCase.getMedicalRecord("record-scheduled");

        // then
        assertEquals(MedicalRecordStatus.RESERVED, result.getStatus());
        assertEquals("예약된 진료", result.getNote());
        verify(loadMedicalRecordViewPort).loadMedicalRecordById("record-scheduled");
    }

    @Test
    @DisplayName("진료기록 시간 범위 검증")
    void should_validateTimeRange_when_getMedicalRecordsByHospital() {
        // given
        String hospitalId = "hospital-1";
        Instant startTime = Instant.now().minusSeconds(86400); // 1일 전
        Instant endTime = Instant.now().minusSeconds(3600); // 1시간 전
        
        when(loadMedicalRecordViewPort.loadMedicalRecordsByHospitalInRange(hospitalId, startTime, endTime))
            .thenReturn(List.of(testMedicalRecord));

        // when
        List<MedicalRecordViewDocument> result = medicalRecordViewUseCase
            .getMedicalRecordsByHospital(hospitalId, startTime, endTime);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(loadMedicalRecordViewPort).loadMedicalRecordsByHospitalInRange(hospitalId, startTime, endTime);
    }
}