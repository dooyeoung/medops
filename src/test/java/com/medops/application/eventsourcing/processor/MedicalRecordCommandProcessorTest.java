package com.medops.application.eventsourcing.processor;

import com.medops.adapter.out.persistence.mongodb.document.*;
import com.medops.adapter.out.persistence.mongodb.repository.*;
import com.medops.application.eventsourcing.command.ConfirmCommand;
import com.medops.application.eventsourcing.command.DoctorAssignCommand;
import com.medops.application.eventsourcing.command.NoteUpdateCommand;
import com.medops.application.eventsourcing.command.ReservationCreatedCommand;
import com.medops.domain.enums.AdminRole;
import com.medops.domain.enums.AdminStatus;
import com.medops.domain.enums.MedicalRecordStatus;
import com.medops.domain.model.MedicalRecordSnapshot;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class MedicalRecordCommandProcessorTest {
    @Autowired
    private MedicalRecordCommandProcessor medicalRecordCommandProcessor;

    @Autowired
    private MedicalRecordEventDocumentRepository medicalRecordEventDocumentRepository;

    @Autowired
    private TreatmentProductDocumentRepository treatmentProductDocumentRepository;

    @Autowired
    private MedicalRecordViewDocumentRepository medicalRecordViewDocumentRepository;

    @Autowired
    private DoctorDocumentRepository doctorDocumentRepository;

    @Autowired
    private AdminDocumentRepository adminDocumentRepository;

    @Autowired
    private UserDocumentRepository userDocumentRepository;

    @Autowired
    private HospitalDocumentRepository hospitalDocumentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String treatmentProductId;
    private String hospitalId;
    private String userId;
    private String adminId;
    private String doctorId;

    @BeforeEach
    void setUp() {
        treatmentProductId = UUID.randomUUID().toString();
        hospitalId = UUID.randomUUID().toString();
        userId = UUID.randomUUID().toString();
        adminId = UUID.randomUUID().toString();
        doctorId = UUID.randomUUID().toString();

        doctorDocumentRepository.save(
            new DoctorDocument(
                doctorId,
                hospitalId,
                "doctor",
                Instant.now(),
                null
            )
        );

        adminDocumentRepository.save(
            new AdminDocument(
                adminId,
                "admin",
                "admin@email.com",
                passwordEncoder.encode("test"),
                AdminRole.ADMIN,
                AdminStatus.ACTIVATED,
                hospitalId,
                Instant.now(),
                null
            )
        );

        hospitalDocumentRepository.save(
            new HospitalDocument(
                hospitalId,
                "test",
                "test",
                Instant.now()
            )
        );

        userDocumentRepository.save(
            new UserDocument(
                userId,
                "test@email.com",
                passwordEncoder.encode("test"),
                "testUser",
                Instant.now()
            )
        );

        treatmentProductDocumentRepository.save(
            new TreatmentProductDocument(
                treatmentProductId,
                hospitalId,
                "test",
                "test",
                1,
                BigDecimal.valueOf(5000),
                Instant.now(),
                null
            )
        );
    }

    @AfterEach
    void tearDown() {
        medicalRecordEventDocumentRepository.deleteAll();
    }

    @Test
    @DisplayName("예약 생성 명령 처리 시 이벤트와 뷰가 정상적으로 저장된다")
    void handleReservationCreatedCommand_ShouldCreateEventAndView() {
        // given
        String recordId = UUID.randomUUID().toString();
        var command = new ReservationCreatedCommand(
            recordId,
            userId,
            hospitalId,
            treatmentProductId,
            LocalDateTime.parse("2025-09-10T10:00:00").toInstant(ZoneOffset.UTC),
            LocalDateTime.parse("2025-09-10T10:30:00").toInstant(ZoneOffset.UTC),
            "기미 고민",
            ""
        );

        // when
        medicalRecordCommandProcessor.handle(command);

        // then
        assertEquals(1, medicalRecordEventDocumentRepository.getAllByRecordId(recordId).size());
        assertEquals(1, medicalRecordViewDocumentRepository.findAllById(List.of(recordId)).size());
    }

    @Test
    @DisplayName("예약 생성 후 확인 처리 시 상태가 변경된다")
    void handleConfirmCommand_AfterReservation_ShouldChangeStatus() {
        // Given - 예약 생성
        String recordId = UUID.randomUUID().toString();
        medicalRecordCommandProcessor.handle(
            new ReservationCreatedCommand(
                recordId,
                userId,
                hospitalId,
                treatmentProductId,
                LocalDateTime.parse("2025-09-10T10:00:00").toInstant(ZoneOffset.UTC),
                LocalDateTime.parse("2025-09-10T10:30:00").toInstant(ZoneOffset.UTC),
                "기미 고민",
                ""
            )
        );

        // When - 확인 처리
        medicalRecordCommandProcessor.handle(
            new ConfirmCommand(
                recordId,
                userId,
                hospitalId,
                adminId
            )
        );

        // Then - 상태 변경 확인
        MedicalRecordSnapshot snapshot = medicalRecordCommandProcessor.rehydrateState(recordId);
        assertEquals(MedicalRecordStatus.RESERVED, snapshot.getState().getStatus());
        assertEquals(2, medicalRecordEventDocumentRepository.getAllByRecordId(recordId).size());
    }

    @Test
    @DisplayName("의사 배정 명령 처리 시 담당 의사가 설정된다")
    void handleDoctorAssignCommand_ShouldAssignDoctor() {
        // Given - 예약 생성 및 확인
        String recordId = UUID.randomUUID().toString();
        medicalRecordCommandProcessor.handle(
            new ReservationCreatedCommand(
                recordId,
                userId,
                hospitalId,
                treatmentProductId,
                LocalDateTime.parse("2025-09-10T10:00:00").toInstant(ZoneOffset.UTC),
                LocalDateTime.parse("2025-09-10T10:30:00").toInstant(ZoneOffset.UTC),
                "기미 고민",
                ""
            )
        );
        medicalRecordCommandProcessor.handle(
            new ConfirmCommand(
                recordId,
                userId,
                hospitalId,
                adminId
            )
        );

        // When - 의사 배정
        medicalRecordCommandProcessor.handle(
            new DoctorAssignCommand(
                recordId,
                userId,
                hospitalId,
                doctorId,
                adminId
            )
        );

        // Then - 담당 의사 설정 확인
        MedicalRecordSnapshot snapshot = medicalRecordCommandProcessor.rehydrateState(recordId);
        assertEquals(doctorId, snapshot.getState().getDoctor());
        assertEquals(3, medicalRecordEventDocumentRepository.getAllByRecordId(recordId).size());
    }

    @Test
    @DisplayName("노트 업데이트 명령 처리 시 메모가 저장된다")
    void handleNoteUpdateCommand_ShouldUpdateNote() {
        // Given - 예약 생성부터 의사 배정까지
        String recordId = UUID.randomUUID().toString();
        String expectedNote = "환자 상태 양호함";
        
        medicalRecordCommandProcessor.handle(
            new ReservationCreatedCommand(
                recordId,
                userId,
                hospitalId,
                treatmentProductId,
                LocalDateTime.parse("2025-09-10T10:00:00").toInstant(ZoneOffset.UTC),
                LocalDateTime.parse("2025-09-10T10:30:00").toInstant(ZoneOffset.UTC),
                "기미 고민",
                ""
            )
        );
        medicalRecordCommandProcessor.handle(
            new ConfirmCommand(
                recordId,
                userId,
                hospitalId,
                adminId
            )
        );
        medicalRecordCommandProcessor.handle(
            new DoctorAssignCommand(
                recordId,
                userId,
                hospitalId,
                doctorId,
                adminId
            )
        );

        // When - 노트 업데이트
        medicalRecordCommandProcessor.handle(
            new NoteUpdateCommand(
                recordId,
                userId,
                hospitalId,
                expectedNote
            )
        );

        // Then - 노트 저장 확인
        MedicalRecordSnapshot snapshot = medicalRecordCommandProcessor.rehydrateState(recordId);
        assertEquals(expectedNote, snapshot.getState().getNote());
        assertEquals(4, medicalRecordEventDocumentRepository.getAllByRecordId(recordId).size());
    }

    @Test
    @DisplayName("연속된 명령 처리 시 스냅샷 재구성이 정확하다")
    void rehydrateState_AfterMultipleCommands_ShouldReturnCorrectSnapshot() {
        // Given - 전체 워크플로우 실행
        String recordId = UUID.randomUUID().toString();
        String finalNote = "최종 진료 메모";
        
        medicalRecordCommandProcessor.handle(
            new ReservationCreatedCommand(
                recordId,
                userId,
                hospitalId,
                treatmentProductId,
                LocalDateTime.parse("2025-09-10T10:00:00").toInstant(ZoneOffset.UTC),
                LocalDateTime.parse("2025-09-10T10:30:00").toInstant(ZoneOffset.UTC),
                "기미 고민",
                ""
            )
        );
        medicalRecordCommandProcessor.handle(
            new ConfirmCommand(
                recordId,
                userId,
                hospitalId,
                adminId
            )
        );
        medicalRecordCommandProcessor.handle(
            new DoctorAssignCommand(
                recordId,
                userId,
                hospitalId,
                doctorId,
                adminId
            )
        );
        medicalRecordCommandProcessor.handle(
            new NoteUpdateCommand(
                recordId,
                userId,
                hospitalId,
                finalNote
            )
        );

        // When - 스냅샷 재구성
        MedicalRecordSnapshot snapshot = medicalRecordCommandProcessor.rehydrateState(recordId);

        // Then - 모든 상태가 정확히 반영됨
        assertEquals(doctorId, snapshot.getState().getDoctor());
        assertEquals(finalNote, snapshot.getState().getNote());
        assertEquals(MedicalRecordStatus.RESERVED, snapshot.getState().getStatus());
        assertEquals(recordId, snapshot.getRecordId());
        assertEquals(4, snapshot.getVersion()); // 4개 이벤트 처리 후 버전
        
        // 이벤트 개수 확인
        assertEquals(4, medicalRecordEventDocumentRepository.getAllByRecordId(recordId).size());
    }
}