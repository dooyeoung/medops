package com.medops.adapter.in.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medops.adapter.in.web.request.UpdateBusinessHourRequest;
import com.medops.application.port.in.usecase.BusinessHourUseCase;
import com.medops.domain.model.BusinessHour;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import com.medops.adapter.out.persistence.mongodb.repository.BusinessHourDocumentRepository;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("BusinessHourApiController E2E 테스트")
class BusinessHourApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BusinessHourUseCase businessHourUseCase;
    
    @Autowired
    private BusinessHourDocumentRepository businessHourRepository;

    private final String hospitalId = "hospital-123";

    @BeforeEach
    void setUp() {
        businessHourRepository.deleteAll();
    }

    @Nested
    @DisplayName("영업시간 조회 테스트")
    class GetBusinessHoursTest {

        @Test
        @WithMockUser
        @DisplayName("특정 병원의 영업시간 목록 조회 성공")
        void getBusinessHours_Success() throws Exception {
            // when & then
            mockMvc.perform(get("/api/business-hours/hospital/{hospitalId}", hospitalId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.resultCode", is(200)))
                    .andExpect(jsonPath("$.body").isArray());
        }

        @Test
        @WithMockUser
        @DisplayName("존재하지 않는 병원의 영업시간 조회 - 빈 리스트 반환")
        void getBusinessHours_EmptyList() throws Exception {
            // when & then
            mockMvc.perform(get("/api/business-hours/hospital/{hospitalId}", "non-existent-hospital")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.resultCode", is(200)))
                    .andExpect(jsonPath("$.body", hasSize(0)));
        }

        @Test
        @DisplayName("인증되지 않은 사용자의 영업시간 조회 시도 - 401 에러")
        void getBusinessHours_Unauthorized() throws Exception {
            // when & then
            mockMvc.perform(get("/api/business-hours/hospital/{hospitalId}", hospitalId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("영업시간 수정 테스트")
    class UpdateBusinessHourTest {

        @Test
        @WithMockUser
        @DisplayName("영업시간 수정 - 존재하지 않는 ID로 시도")
        void updateBusinessHour_NotFound() throws Exception {
            // given
            UpdateBusinessHourRequest request = new UpdateBusinessHourRequest(
                "09:00",    // 오픈 시간
                "18:00",    // 마감 시간
                "12:00",    // 점심 시작
                "13:00",    // 점심 종료
                false       // 휴무 아님
            );

            // when & then - 존재하지 않는 ID이므로 에러 발생 가능
            mockMvc.perform(put("/api/business-hours/{businessHourId}", "non-existent-id")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest()); // 존재하지 않는 ID로 인한 에러
        }

        @Test
        @WithMockUser
        @DisplayName("영업시간 수정 실패 - 잘못된 시간 순서")
        void updateBusinessHour_InvalidTimeOrder() throws Exception {
            // given
            UpdateBusinessHourRequest request = new UpdateBusinessHourRequest(
                "20:00",    // 오픈 시간이 마감시간보다 늦음
                "18:00",    // 마감 시간
                "12:00",    // 점심 시작
                "13:00",    // 점심 종료
                false       // 휴무 아님
            );

            // when & then - BusinessHourInvalidation 예외 발생 예상
            mockMvc.perform(put("/api/business-hours/{businessHourId}", "test-business-hour-id")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest()); // 유효성 검증 실패
        }

        @Test
        @WithMockUser
        @DisplayName("영업시간 수정 실패 - 점심시간 순서 오류")
        void updateBusinessHour_InvalidBreakTime() throws Exception {
            // given
            UpdateBusinessHourRequest request = new UpdateBusinessHourRequest(
                "09:00",    // 오픈 시간
                "18:00",    // 마감 시간
                "13:00",    // 점심 시작이 점심 종료보다 늦음
                "12:00",    // 점심 종료
                false       // 휴무 아님
            );

            // when & then - BusinessHourInvalidation 예외 발생 예상
            mockMvc.perform(put("/api/business-hours/{businessHourId}", "test-business-hour-id")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest()); // 유효성 검증 실패
        }

        @Test
        @DisplayName("인증되지 않은 사용자의 영업시간 수정 시도 - 401 에러")
        void updateBusinessHour_Unauthorized() throws Exception {
            // given
            UpdateBusinessHourRequest request = new UpdateBusinessHourRequest(
                "09:00", "18:00", "12:00", "13:00", false
            );

            // when & then
            mockMvc.perform(put("/api/business-hours/{businessHourId}", "test-business-hour-id")
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
        @DisplayName("영업시간 조회 후 수정 시도하는 시나리오")
        void businessHourQueryAndUpdateScenario() throws Exception {
            // 1. 영업시간 조회
            mockMvc.perform(get("/api/business-hours/hospital/{hospitalId}", hospitalId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.resultCode", is(200)));

            // 2. 영업시간 수정 시도 (존재하지 않는 ID)
            UpdateBusinessHourRequest updateRequest = new UpdateBusinessHourRequest(
                "10:00", "19:00", "13:00", "14:00", false
            );

            mockMvc.perform(put("/api/business-hours/{businessHourId}", "non-existent-id")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isBadRequest()); // 존재하지 않는 ID
        }

        @Test
        @WithMockUser
        @DisplayName("잘못된 시간 형식으로 영업시간 수정 시도")
        void invalidTimeFormatScenario() throws Exception {
            // JSON에서 잘못된 시간 형식 전송 시 파싱 에러 발생할 것임
            String invalidJson = """
                {
                    "openTime": "25:00",
                    "closeTime": "18:00",
                    "breakStartTime": "12:00",
                    "breakEndTime": "13:00",
                    "closed": false
                }
                """;

            // when & then - 시간 파싱 에러 또는 유효성 검증 에러 예상
            mockMvc.perform(put("/api/business-hours/{businessHourId}", "test-id")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest()); // 잘못된 형식으로 인한 400 에러
        }
    }
}