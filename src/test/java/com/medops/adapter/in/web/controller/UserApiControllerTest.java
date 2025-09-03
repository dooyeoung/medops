package com.medops.adapter.in.web.controller;

import com.medops.adapter.in.web.request.UserLoginRequest;
import com.medops.adapter.in.web.request.UserRegisterRequest;
import com.medops.application.port.in.usecase.UserUseCase;
import com.medops.application.port.out.SaveUserPort;
import com.medops.domain.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserApiControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserUseCase userUseCase;

    @Autowired
    private SaveUserPort saveUserPort;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    @DisplayName("사용자 회원가입 성공 시 200 OK와 사용자 정보를 반환한다")
    void createUser_succeeds() throws Exception {
        // given
        String testEmail = "test7760@test.com";
        String testPassword = "1234!@#$";
        String testName = "testUser";

        // when & then
        mockMvc.perform(
            post("/api/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        new UserRegisterRequest(testEmail, testPassword, testName)
                    )
                )
            )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.body.email").value(testEmail));
    }

    @Test
    @DisplayName("유효한 JWT 토큰으로 내 정보 조회 시 200 OK와 사용자 정보를 반환한다")
    void getUserProfile_withValidToken_succeeds() throws Exception {
        // given
        String testEmail = "test2@test.com";
        String testPassword = "1234!@#$";
        saveUserPort.saveUser(
            User.builder().id(UUID.randomUUID().toString()).email(testEmail).password(testPassword).build()
        );
        String jwtToken = userUseCase.loginUser(new UserLoginRequest(testEmail, testPassword));

        // when & then
        mockMvc.perform(
                get("/api/user/me")
                    .header("Authorization", "Bearer " + jwtToken)
                    .contentType(MediaType.APPLICATION_JSON)
            ).andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("잘못된 JWT 토큰으로 내 정보 조회 시 401 Unauthorized를 반환한다")
    void getUserProfile_withInvalidToken_returns401() throws Exception {
        // given
        String invalidToken = "invalidToken";

        // when & then
        mockMvc.perform(
                get("/api/user/me")
                    .header("Authorization", "Bearer " + invalidToken)
                    .contentType(MediaType.APPLICATION_JSON)
            ).andDo(print())
            .andExpect(status().isUnauthorized());
    }


    @Test
    @DisplayName("JWT 토큰 없이 내 정보 조회 시 401 Unauthorized를 반환한다")
    void getUserProfile_withoutToken_returns401() throws Exception {
        // when & then
        mockMvc.perform(
                get("/api/user/me")
                    .contentType(MediaType.APPLICATION_JSON)
            ).andDo(print())
            .andExpect(status().isUnauthorized());
    }

}