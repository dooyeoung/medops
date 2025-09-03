package com.medops.adapter.in.web.controller;

import com.medops.adapter.in.web.request.HospitalCreateRequest;
import com.medops.adapter.in.web.request.UserLoginRequest;
import com.medops.adapter.out.persistence.mongodb.document.HospitalDocument;
import com.medops.application.port.in.usecase.HospitalUseCase;
import com.medops.application.port.in.usecase.UserUseCase;
import com.medops.application.port.out.SaveUserPort;
import com.medops.domain.model.Hospital;
import com.medops.domain.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class HospitalApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HospitalUseCase hospitalUseCase;

    @Autowired
    private SaveUserPort saveUserPort;

    @Autowired
    private UserUseCase userUseCase;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MongoTemplate mongoTemplate;

    private String testHospitalName;
    private String testAddress;
    private String testAdminEmail;
    private String testUserEmail;
    private String testPassword;
    private Hospital testHospital;

    @BeforeEach
    void setUp() {
        // MongoDB 컬렉션 정리
        // mongoTemplate.getDb().drop();
        mongoTemplate.remove(new Query(), HospitalDocument.class);

        // 각 테스트마다 고유한 데이터 생성
        String timestamp = String.valueOf(System.currentTimeMillis());
        testHospitalName = "테스트병원_" + timestamp;
        testAddress = "강남구";
        testAdminEmail = "admin_" + timestamp + "@test.com";
        testUserEmail = "test_" + timestamp + "@test.com";
        testPassword = "password123";
        
        // 테스트용 병원 생성
        testHospital = hospitalUseCase.createHospital(
            new HospitalCreateRequest(testHospitalName, testAddress, testAdminEmail, "관리자", testPassword)
        );
        
        // 테스트 사용자 생성
        saveUserPort.saveUser(
            User.builder()
                .id(UUID.randomUUID().toString())
                .email(testUserEmail)
                .password(testPassword)
                .build()
        );
    }

    @Test
    @DisplayName("병원 생성 성공 시 200 OK와 생성된 병원 정보를 반환한다")
    void createHospital_succeeds() throws Exception {
        // Given
        String timestamp = String.valueOf(System.currentTimeMillis());
        String hospitalName = "새로운병원_" + timestamp;
        String address = "서울시";
        String adminEmail = "newadmin_" + timestamp + "@test.com";
        String adminName = "관리자";
        String adminPassword = "password";

        // When & Then
        mockMvc.perform(
            post("/api/hospital")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        new HospitalCreateRequest(hospitalName, address, adminEmail, adminName, adminPassword)
                    )
                )
            )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.body.name").value(hospitalName))
            .andExpect(jsonPath("$.body.address").value(address));
    }

    @Test
    @DisplayName("전체 병원 목록 조회 성공 시 200 OK와 병원 배열을 반환한다")
    void getAllHospitals_succeeds() throws Exception {
        // Given
        String jwtToken = userUseCase.loginUser(new UserLoginRequest(testUserEmail, testPassword));

        // When & Then
        mockMvc.perform(
                get("/api/hospital")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
            )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.body").isArray())
            .andExpect(jsonPath("$.body.length()").value(1))
            .andExpect(jsonPath("$.body[0].name").value(testHospitalName))
            .andExpect(jsonPath("$.body[0].address").value(testAddress));
    }

    @Test
    @DisplayName("유효한 ID로 병원 조회 성공 시 200 OK와 병원 정보를 반환한다")
    void test_get_hospital() throws Exception {
        // Given
        String jwtToken = userUseCase.loginUser(new UserLoginRequest(testUserEmail, testPassword));

        // When & Then
        mockMvc.perform(
            get("/api/hospital/{hospitalId}", testHospital.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.body.name").value(testHospitalName))
        .andExpect(jsonPath("$.body.address").value(testAddress));
    }

    @Test
    @DisplayName("존재하지 않는 ID로 병원 조회 시 400 Bad Request를 반환한다")
    void test_get_hospital_wrong_Id() throws Exception {
        // Given
        String jwtToken = userUseCase.loginUser(new UserLoginRequest(testUserEmail, testPassword));
        String invalidHospitalId = "invalid-hospital-id-" + System.currentTimeMillis();

        // When & Then
        mockMvc.perform(
                get("/api/hospital/{hospitalId}", invalidHospitalId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
            )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.result.resultMessage").value("잘못된 요청"))
            .andExpect(jsonPath("$.result.resultDescription").value("병원을 조회할 수 없습니다."));
    }
}