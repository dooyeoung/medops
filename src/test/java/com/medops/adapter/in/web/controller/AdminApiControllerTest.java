package com.medops.adapter.in.web.controller;

import com.medops.adapter.in.web.request.InviteAdminRequest;
import com.medops.adapter.in.web.request.AdminLoginRequest;
import com.medops.adapter.in.web.request.HospitalCreateRequest;
import com.medops.adapter.in.web.request.VerifyAdminInvitationCodeRequest;
import com.medops.application.port.in.usecase.AdminUseCase;
import com.medops.application.port.in.usecase.HospitalUseCase;
import com.medops.application.port.in.command.AdminInviteCommand;
import com.medops.application.port.out.LoadAdminRegistrationTokenPort;
import com.medops.application.port.out.LoadAdminPort;
import com.medops.application.port.out.VerificationCodePort;
import com.medops.domain.model.Admin;
import com.medops.domain.model.AdminRegistrationToken;
import com.medops.domain.model.Hospital;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // 1. 클래스 단위로 인스턴스 생성
public class AdminApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HospitalUseCase hospitalUseCase;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AdminUseCase adminUseCase;

    @Autowired
    private VerificationCodePort verificationCodePort;

    @Autowired
    private LoadAdminPort loadAdminPort;

    @Autowired
    private LoadAdminRegistrationTokenPort loadAdminRegistrationTokenPort;

    private String adminEmail = "admin@email.com";
    private String adminName = "관리자";
    private String adminPassword = "password";
    private String hospitalName = "테스트병원65";

    @BeforeAll
    void setup() {
        hospitalUseCase.createHospital(
            new HospitalCreateRequest(hospitalName, "강남구", adminEmail, adminName, adminPassword)
        );
    }

    @Test
    @DisplayName("관리자 로그인 성공 시 200 OK와 JWT 토큰을 반환한다")
    void adminLogin_succeeds() throws Exception {
        mockMvc.perform(
            post("/api/admin/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        new AdminLoginRequest(adminEmail, adminPassword, hospitalName)
                    )
                )
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().string(not(blankString())));
    }

    @Test
    @DisplayName("잘못된 비밀번호로 관리자 로그인 시 400 Bad Request를 반환한다")
    void adminLogin_withWrongPassword_returns400() throws Exception {
        // Given: 잘못된 비밀번호를 포함한 로그인 요청
        String wrongPassword = "wrong_password";
        AdminLoginRequest loginRequest = new AdminLoginRequest(adminEmail, wrongPassword, hospitalName);

        // When & Then: 401 Unauthorized 상태를 반환하는지 검증
        mockMvc.perform(
                post("/api/admin/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest))
            )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.result.resultMessage").value("잘못된 요청"))
            .andExpect(jsonPath("$.result.resultDescription").value("패스워드가 일치하지 않습니다."));
    }

    @Test
    @DisplayName("유효한 JWT 토큰으로 관리자 정보 조회 시 200 OK와 관리자 정보를 반환한다")
    void getAdminProfile_withValidToken_succeeds() throws Exception {
        String jwtToken = adminUseCase.loginAdmin(
            new AdminLoginRequest(adminEmail, adminPassword, hospitalName)
        );

        mockMvc.perform(
                get("/api/admin/me")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
            )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.body.name").value(adminName))
            .andExpect(jsonPath("$.body.email").value(adminEmail))
            .andExpect(jsonPath("$.body.hospital.name").value(hospitalName));
    }

    @Test
    @DisplayName("JWT 토큰 없이 관리자 정보 조회 시 401 Unauthorized를 반환한다")
    void getAdminProfile_withoutToken_returns401() throws Exception {
        // Given: Authorization 헤더가 없는 요청

        // When & Then: 401 Unauthorized 상태를 반환하는지 검증
        mockMvc.perform(get("/api/admin/me"))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("관리자 초대 성공 시 200 OK와 초대 코드를 생성한다")
    void inviteAdmin_succeeds() throws Exception {
        String otherAdmin = "newadmin@email.com";

        String jwtToken = adminUseCase.loginAdmin(
            new AdminLoginRequest(adminEmail, adminPassword, hospitalName)
        );

        mockMvc.perform(
            post("/api/admin/invite")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        new InviteAdminRequest(otherAdmin)
                    )
                )
        )
        .andDo(print())
        .andExpect(status().isOk());

        String invitationCode = verificationCodePort.getInvitationCode(otherAdmin).code();
        assertNotNull(invitationCode, "초대 코드가 null이 아니어야 합니다.");
    }

    @Test
    @DisplayName("유효한 초대 코드로 검증 성공 시 200 OK와 등록 토큰을 생성한다")
    void verifyInvitationCode_withValidCode_succeeds() throws Exception {
        // Given: 유효한 초대 코드 미리 생성
        String targetAdminEmail = "verifiedadmin@email.com";

        Hospital hospital = hospitalUseCase.getHospitalByName(hospitalName).orElseThrow();

        adminUseCase.inviteAdmin(
            new AdminInviteCommand(targetAdminEmail, hospital.getId())
        );
        String validCode = verificationCodePort.getInvitationCode(targetAdminEmail).code();

        // When: 생성된 코드로 초대 코드 검증 API 호출
        mockMvc.perform(
            post("/api/admin/verify-invitation-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        new VerifyAdminInvitationCodeRequest(targetAdminEmail, validCode)
                    )
                )
        )
        .andDo(print())
        .andExpect(status().isOk());

        Admin admin = loadAdminPort.loadAdminByEmail(targetAdminEmail).orElseThrow();
        AdminRegistrationToken token = loadAdminRegistrationTokenPort.loadAdminRegistrationTokenByAdminId(admin.getId()).orElseThrow();
        assertNotNull(token.getRegistrationToken());
    }
}
