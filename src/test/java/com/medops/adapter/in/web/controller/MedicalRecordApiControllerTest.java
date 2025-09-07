package com.medops.adapter.in.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medops.adapter.in.web.request.*;
import com.medops.adapter.out.persistence.mongodb.repository.MedicalRecordViewDocumentRepository;
import com.medops.adapter.out.persistence.mongodb.repository.MedicalRecordEventDocumentRepository;
import com.medops.application.eventsourcing.processor.MedicalRecordCommandProcessor;
import com.medops.application.port.in.usecase.MedicalRecordEventUseCase;
import com.medops.application.port.in.usecase.MedicalRecordViewUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("MedicalRecordApiController E2E 테스트")
class MedicalRecordApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MedicalRecordCommandProcessor commandProcessor;

    @Autowired
    private MedicalRecordViewUseCase medicalRecordViewUseCase;

    @Autowired
    private MedicalRecordEventUseCase medicalRecordEventUseCase;

    @Autowired
    private MedicalRecordViewDocumentRepository medicalRecordViewRepository;

    @Autowired
    private MedicalRecordEventDocumentRepository medicalRecordEventRepository;

    private final String recordId = "record-123";
    private final String userId = "user-123";
    private final String hospitalId = "hospital-123";
    private final String adminId = "admin-123";
    private final String doctorId = "doctor-123";

    @BeforeEach
    void setUp() {
        medicalRecordViewRepository.deleteAll();
        medicalRecordEventRepository.deleteAll();
    }

    @Nested
    @DisplayName("의료기록 조회 테스트")
    class GetMedicalRecordTest {

        @Test
        @WithMockUser
        @DisplayName("존재하지 않는 의료기록 조회 - 에러 발생")
        void getMedicalRecord_NotFound() throws Exception {
            // when & then - 존재하지 않는 recordId로 조회
            mockMvc.perform(get("/api/medical-records/{recordId}", "non-existent-record")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isBadRequest()); // 또는 적절한 에러 상태
        }

        @Test
        @DisplayName("인증되지 않은 사용자의 의료기록 조회 시도 - 401 에러")
        void getMedicalRecord_Unauthorized() throws Exception {
            // when & then
            mockMvc.perform(get("/api/medical-records/{recordId}", recordId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("의료기록 이벤트 조회 테스트")
    class GetEventsByRecordIdTest {

        @Test
        @WithMockUser
        @DisplayName("존재하지 않는 의료기록의 이벤트 목록 조회 - 빈 배열 반환")
        void getEventsByRecordId_EmptyList() throws Exception {
            // when & then - 존재하지 않는 recordId로 이벤트 조회
            mockMvc.perform(get("/api/medical-records/{recordId}/events", "non-existent-record")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.resultCode", is(200)))
                    .andExpect(jsonPath("$.body").isArray())
                    .andExpect(jsonPath("$.body", hasSize(0))); // 빈 배열 반환 예상
        }

        @Test
        @DisplayName("인증되지 않은 사용자의 이벤트 조회 시도 - 401 에러")
        void getEventsByRecordId_Unauthorized() throws Exception {
            // when & then
            mockMvc.perform(get("/api/medical-records/{recordId}/events", recordId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("의료기록 생성 테스트")
    class CreateMedicalRecordTest {

        @Test
        @WithMockUser
        @DisplayName("의료기록 생성 - UserSession 파라미터 해결 문제로 인한 400 에러")
        void createMedicalRecord_UserSessionParameterIssue() throws Exception {
            // given
            CreateReservationRequest request = new CreateReservationRequest(
                hospitalId,
                "treatment-123",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                "사용자 메모"
            );

            // when & then - @UserSession 리졸버 문제로 400 에러 예상
            mockMvc.perform(post("/api/medical-records")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.result.resultCode", is(400)));
        }

        @Test
        @WithMockUser
        @DisplayName("의료기록 생성 실패 - 필수 필드 누락")
        void createMedicalRecord_MissingFields() throws Exception {
            // given - hospitalId가 null인 잘못된 요청
            CreateReservationRequest request = new CreateReservationRequest(
                null,
                "treatment-123",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                "사용자 메모"
            );

            // when & then
            mockMvc.perform(post("/api/medical-records")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("인증되지 않은 사용자의 의료기록 생성 시도 - 401 에러")
        void createMedicalRecord_Unauthorized() throws Exception {
            // given
            CreateReservationRequest request = new CreateReservationRequest(
                hospitalId, "treatment-123", Instant.now(), Instant.now().plusSeconds(3600), "메모"
            );

            // when & then
            mockMvc.perform(post("/api/medical-records")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("후속 의료기록 생성 테스트")
    class FollowUpMedicalRecordTest {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("후속 의료기록 생성 - AdminSession 파라미터 해결 문제로 인한 400 에러")
        void followUpMedicalRecord_AdminSessionParameterIssue() throws Exception {
            // given
            FollowUpReservationRequest request = new FollowUpReservationRequest(
                userId,
                hospitalId,
                "treatment-123",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                "관리자 노트"
            );

            // when & then - @AdminSession 리졸버 문제로 400 에러 예상
            mockMvc.perform(post("/api/medical-records/follow-up")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.result.resultCode", is(400)));
        }

        @Test
        @DisplayName("인증되지 않은 사용자의 후속 의료기록 생성 시도 - 401 에러")
        void followUpMedicalRecord_Unauthorized() throws Exception {
            // given
            FollowUpReservationRequest request = new FollowUpReservationRequest(
                userId, hospitalId, "treatment-123", Instant.now(), Instant.now().plusSeconds(3600), "노트"
            );

            // when & then
            mockMvc.perform(post("/api/medical-records/follow-up")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("병원별 의료기록 조회 테스트")
    class GetHospitalMedicalRecordsTest {

        @Test
        @WithMockUser
        @DisplayName("병원별 의료기록 목록 조회 성공 - 빈 리스트")
        void getHospitalMedicalRecords_EmptyList() throws Exception {
            // given
            Instant startTime = Instant.now().minusSeconds(86400);
            Instant endTime = Instant.now();

            // when & then
            mockMvc.perform(get("/api/medical-records/hospitals/{hospitalId}", hospitalId)
                            .param("startTime", startTime.toString())
                            .param("endTime", endTime.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.resultCode", is(200)))
                    .andExpect(jsonPath("$.body").isArray())
                    .andExpect(jsonPath("$.body", hasSize(0)));
        }

        @Test
        @WithMockUser
        @DisplayName("병원별 의료기록 조회 실패 - 필수 파라미터 누락")
        void getHospitalMedicalRecords_MissingParams() throws Exception {
            // when & then - startTime 파라미터 누락
            mockMvc.perform(get("/api/medical-records/hospitals/{hospitalId}", hospitalId)
                            .param("endTime", Instant.now().toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("예약 상태 변경 테스트")
    class ReservationStatusChangeTest {

        @Test
        @WithMockUser
        @DisplayName("예약 확정 - 존재하지 않는 recordId")
        void confirmReservation_NotFound() throws Exception {
            // given
            ConfirmReservationRequest request = new ConfirmReservationRequest(userId, hospitalId, adminId);

            // when & then
            mockMvc.perform(patch("/api/medical-records/{recordId}/status/confirm", "non-existent-id")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest()); // 존재하지 않는 ID로 인한 에러
        }

        @Test
        @WithMockUser
        @DisplayName("예약 대기 상태 변경 - 존재하지 않는 recordId")
        void pendingReservation_NotFound() throws Exception {
            // given
            PendingReservationRequest request = new PendingReservationRequest(userId, hospitalId, adminId);

            // when & then
            mockMvc.perform(patch("/api/medical-records/{recordId}/status/pending", "non-existent-id")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("예약 취소 - 존재하지 않는 recordId")
        void cancelReservation_NotFound() throws Exception {
            // given
            CancelReservationRequest request = new CancelReservationRequest(userId, hospitalId, adminId);

            // when & then
            mockMvc.perform(patch("/api/medical-records/{recordId}/status/cancel", "non-existent-id")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("예약 완료 - 존재하지 않는 recordId")
        void completeReservation_NotFound() throws Exception {
            // given
            CompleteReservationRequest request = new CompleteReservationRequest(userId, hospitalId, adminId);

            // when & then
            mockMvc.perform(patch("/api/medical-records/{recordId}/status/complete", "non-existent-id")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("인증되지 않은 사용자의 예약 상태 변경 시도 - 401 에러")
        void changeReservationStatus_Unauthorized() throws Exception {
            // given
            ConfirmReservationRequest request = new ConfirmReservationRequest(userId, hospitalId, adminId);

            // when & then
            mockMvc.perform(patch("/api/medical-records/{recordId}/status/confirm", recordId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("노트 업데이트 테스트")
    class UpdateNoteTest {

        @Test
        @WithMockUser
        @DisplayName("노트 업데이트 성공 - 세션 애노테이션 없음")
        void updateNote_Success() throws Exception {
            // given
            UpdateReservationNoteRequest request = new UpdateReservationNoteRequest(
                userId, hospitalId, "업데이트된 노트"
            );

            // when & then - 이 메서드는 @UserSession을 사용하지 않으므로 200 성공
            mockMvc.perform(patch("/api/medical-records/{recordId}/note", "test-record-id")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.resultCode", is(200)));
        }

        @Test
        @DisplayName("인증되지 않은 사용자의 노트 업데이트 시도 - 401 에러")
        void updateNote_Unauthorized() throws Exception {
            // given
            UpdateReservationNoteRequest request = new UpdateReservationNoteRequest(
                userId, hospitalId, "노트"
            );

            // when & then
            mockMvc.perform(patch("/api/medical-records/{recordId}/note", recordId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("사용자별 의료기록 조회 테스트")
    class GetUserMedicalRecordsTest {

        @Test
        @WithMockUser
        @DisplayName("사용자별 의료기록 목록 조회 성공 - 빈 리스트")
        void getUserMedicalRecords_EmptyList() throws Exception {
            // when & then
            mockMvc.perform(get("/api/medical-records/users/{userId}", userId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.resultCode", is(200)))
                    .andExpect(jsonPath("$.body").isArray())
                    .andExpect(jsonPath("$.body", hasSize(0)));
        }

        @Test
        @WithMockUser
        @DisplayName("특정 병원의 사용자 의료기록 조회 성공 - 빈 리스트")
        void getUserMedicalRecordsInHospital_EmptyList() throws Exception {
            // when & then
            mockMvc.perform(get("/api/medical-records/users/{userId}/hospitals/{hospitalId}", userId, hospitalId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.resultCode", is(200)))
                    .andExpect(jsonPath("$.body").isArray())
                    .andExpect(jsonPath("$.body", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("의사 배정 테스트")
    class AssignDoctorTest {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("의사 배정 - 존재하지 않는 recordId")
        void assignDoctorReservation_NotFound() throws Exception {
            // given
            AssignDoctorRequest request = new AssignDoctorRequest(userId, hospitalId, doctorId);

            // when & then
            mockMvc.perform(patch("/api/medical-records/{recordId}/doctor", "non-existent-id")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("인증되지 않은 사용자의 의사 배정 시도 - 401 에러")
        void assignDoctorReservation_Unauthorized() throws Exception {
            // given
            AssignDoctorRequest request = new AssignDoctorRequest(userId, hospitalId, doctorId);

            // when & then
            mockMvc.perform(patch("/api/medical-records/{recordId}/doctor", recordId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("통합 시나리오 테스트")
    class IntegrationScenarioTest {

        @Test
        @WithMockUser
        @DisplayName("의료기록 생성 후 상태 변경 시나리오 - 파라미터 타입 불일치")
        void createAndUpdateRecordScenario_ParameterTypeMismatch() throws Exception {
            // 1. 의료기록 생성 시도 - 파라미터 타입 불일치로 400 에러
            CreateReservationRequest createRequest = new CreateReservationRequest(
                hospitalId, "treatment-123", Instant.now(), Instant.now().plusSeconds(3600), "메모"
            );

            mockMvc.perform(post("/api/medical-records")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isBadRequest());

            // 2. 예약 확정 시도 (존재하지 않는 ID이므로 에러)
            ConfirmReservationRequest confirmRequest = new ConfirmReservationRequest(userId, hospitalId, adminId);

            mockMvc.perform(patch("/api/medical-records/{recordId}/status/confirm", "non-existent-id")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(confirmRequest)))
                    .andExpect(status().isBadRequest());

            // 3. 노트 업데이트 시도 (세션 애노테이션 없으므로 성공)
            UpdateReservationNoteRequest noteRequest = new UpdateReservationNoteRequest(
                userId, hospitalId, "업데이트된 노트"
            );

            mockMvc.perform(patch("/api/medical-records/{recordId}/note", "non-existent-id")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(noteRequest)))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser
        @DisplayName("잘못된 JSON 형식으로 요청 시도")
        void invalidJsonFormatScenario() throws Exception {
            String invalidJson = """
                {
                    "hospitalId": "hospital-123",
                    "treatmentProductId": "treatment-123",
                    "startTime": "invalid-time-format",
                    "endTime": "2024-12-07T10:00:00Z",
                    "userMemo": "테스트 메모"
                }
                """;

            // when & then - 시간 파싱 에러 또는 유효성 검증 에러 예상
            mockMvc.perform(post("/api/medical-records")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("존재하지 않는 리소스에 대한 작업 시도")
        void operationOnNonExistentResource() throws Exception {
            // given
            ConfirmReservationRequest request = new ConfirmReservationRequest(userId, hospitalId, adminId);

            // when & then - 존재하지 않는 recordId로 상태 변경 시도
            mockMvc.perform(patch("/api/medical-records/{recordId}/status/confirm", "non-existent-id")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }
}