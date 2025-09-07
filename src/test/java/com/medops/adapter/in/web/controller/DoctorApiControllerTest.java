package com.medops.adapter.in.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medops.adapter.in.web.request.CreateDoctorRequest;
import com.medops.adapter.in.web.request.UpdateDoctorRequest;
import com.medops.application.port.in.command.CreateDoctorCommand;
import com.medops.application.port.in.command.DeleteDoctorCommand;
import com.medops.application.port.in.usecase.DoctorUseCase;
import com.medops.domain.model.Doctor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import com.medops.adapter.out.persistence.mongodb.repository.DoctorDocumentRepository;
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
@DisplayName("DoctorApiController E2E 테스트")
class DoctorApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DoctorUseCase doctorUseCase;
    
    @Autowired
    private DoctorDocumentRepository doctorRepository;

    private final String hospitalId = "hospital-123";

    @BeforeEach
    void setUp() {
        doctorRepository.deleteAll();
    }

    @Nested
    @DisplayName("의사 조회 테스트")
    class GetDoctorsTest {

        @Test
        @WithMockUser
        @DisplayName("특정 병원의 의사 목록 조회 성공")
        void getDoctorsByHospitalId_Success() throws Exception {
            // given - 실제 데이터 생성
            doctorUseCase.createDoctor(new CreateDoctorCommand(hospitalId, "김의사"));
            doctorUseCase.createDoctor(new CreateDoctorCommand(hospitalId, "박의사"));

            // when & then
            mockMvc.perform(get("/api/doctor/hospitals/{hospitalId}", hospitalId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.resultCode", is(200)))
                    .andExpect(jsonPath("$.body", hasSize(2)))
                    .andExpect(jsonPath("$.body[0].name", is("김의사")))
                    .andExpect(jsonPath("$.body[0].hospitalId", is(hospitalId)))
                    .andExpect(jsonPath("$.body[1].name", is("박의사")))
                    .andExpect(jsonPath("$.body[1].hospitalId", is(hospitalId)));
        }

        @Test
        @WithMockUser
        @DisplayName("존재하지 않는 병원의 의사 목록 조회 - 빈 리스트 반환")
        void getDoctorsByHospitalId_EmptyList() throws Exception {
            // given - 아무 데이터도 생성하지 않음

            // when & then
            mockMvc.perform(get("/api/doctor/hospitals/{hospitalId}", "non-existent-hospital")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.resultCode", is(200)))
                    .andExpect(jsonPath("$.body", hasSize(0)));
        }

        @Test
        @DisplayName("인증되지 않은 사용자의 의사 목록 조회 시도 - 401 에러")
        void getDoctorsByHospitalId_Unauthorized() throws Exception {
            // when & then
            mockMvc.perform(get("/api/doctor/hospitals/{hospitalId}", hospitalId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("의사 생성 테스트")
    class CreateDoctorTest {

        @Test
        @WithMockUser
        @DisplayName("의사 생성 성공")
        void createDoctor_Success() throws Exception {
            // given
            CreateDoctorRequest request = new CreateDoctorRequest(hospitalId, "새의사");

            // when
            mockMvc.perform(post("/api/doctor")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.resultCode", is(200)))
                    .andExpect(jsonPath("$.body").doesNotExist());

            // then - 실제 데이터가 생성되었는지 확인
            List<Doctor> doctors = doctorUseCase.getDoctorsByHospitalId(hospitalId);
            assert doctors.size() == 1;
            assert doctors.get(0).getName().equals("새의사");
        }

        @Test
        @DisplayName("인증되지 않은 사용자의 의사 생성 시도 - 401 에러")
        void createDoctor_Unauthorized() throws Exception {
            // given
            CreateDoctorRequest request = new CreateDoctorRequest(hospitalId, "새의사");

            // when & then
            mockMvc.perform(post("/api/doctor")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("의사 수정 테스트")
    class UpdateDoctorTest {

        @Test
        @WithMockUser
        @DisplayName("의사 정보 수정 성공")
        void updateDoctor_Success() throws Exception {
            // given - 먼저 의사를 생성
            doctorUseCase.createDoctor(new CreateDoctorCommand(hospitalId, "원래의사"));
            List<Doctor> doctors = doctorUseCase.getDoctorsByHospitalId(hospitalId);
            String createdDoctorId = doctors.get(0).getId();
            
            UpdateDoctorRequest request = new UpdateDoctorRequest("수정된의사");

            // when & then
            mockMvc.perform(put("/api/doctor/{doctorId}", createdDoctorId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.resultCode", is(200)))
                    .andExpect(jsonPath("$.body").doesNotExist());
        }
    }

    @Nested
    @DisplayName("의사 삭제 테스트")
    class DeleteDoctorTest {

        @Test
        @WithMockUser
        @DisplayName("의사 삭제 성공")
        void deleteDoctor_Success() throws Exception {
            // given - 먼저 의사를 생성
            doctorUseCase.createDoctor(new CreateDoctorCommand(hospitalId, "삭제될의사"));
            List<Doctor> doctors = doctorUseCase.getDoctorsByHospitalId(hospitalId);
            String createdDoctorId = doctors.get(0).getId();

            // when & then
            mockMvc.perform(delete("/api/doctor/{doctorId}", createdDoctorId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.resultCode", is(200)))
                    .andExpect(jsonPath("$.body").doesNotExist());
        }
    }

    @Nested
    @DisplayName("의사 복구 테스트")
    class RecoverDoctorTest {

        @Test
        @WithMockUser
        @DisplayName("의사 복구 성공")
        void recoverDoctor_Success() throws Exception {
            // given - 의사를 생성하고 삭제
            doctorUseCase.createDoctor(new CreateDoctorCommand(hospitalId, "복구될의사"));
            List<Doctor> doctors = doctorUseCase.getDoctorsByHospitalId(hospitalId);
            String createdDoctorId = doctors.get(0).getId();
            doctorUseCase.deleteDoctor(new DeleteDoctorCommand(createdDoctorId));

            // when & then
            mockMvc.perform(patch("/api/doctor/{doctorId}/recover", createdDoctorId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.resultCode", is(200)))
                    .andExpect(jsonPath("$.body").doesNotExist());
        }
    }

    @Nested
    @DisplayName("통합 시나리오 테스트")
    class IntegrationScenarioTest {

        @Test
        @WithMockUser
        @DisplayName("의사 생성 -> 조회 -> 수정 -> 삭제 -> 복구 전체 시나리오")
        void doctorLifecycleScenario() throws Exception {
            // 1. 의사 생성
            CreateDoctorRequest createRequest = new CreateDoctorRequest(hospitalId, "시나리오의사");
            mockMvc.perform(post("/api/doctor")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isOk());

            // 2. 의사 목록 조회
            mockMvc.perform(get("/api/doctor/hospitals/{hospitalId}", hospitalId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.body", hasSize(1)));

            // 생성된 의사의 ID 조회
            List<Doctor> doctors = doctorUseCase.getDoctorsByHospitalId(hospitalId);
            String createdDoctorId = doctors.get(0).getId();

            // 3. 의사 수정
            UpdateDoctorRequest updateRequest = new UpdateDoctorRequest("수정된시나리오의사");
            mockMvc.perform(put("/api/doctor/{doctorId}", createdDoctorId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk());

            // 4. 의사 삭제
            mockMvc.perform(delete("/api/doctor/{doctorId}", createdDoctorId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // 5. 의사 복구
            mockMvc.perform(patch("/api/doctor/{doctorId}/recover", createdDoctorId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }
}