package com.medops.adapter.in.web.controller;

import com.medops.application.service.SseEmitterService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SseEmitterService sseEmitterService;

    @Test
    @DisplayName("SSE 알림 구독 - 관리자 세션 없이 호출시 400 에러")
    @WithMockUser
    void subscribeNotifications_NoAdminSession_Returns400() throws Exception {
        // given & when & then
        mockMvc.perform(get("/api/admin/notifications/stream"))
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("POST 메소드로 SSE 구독 시도 - 4xx 에러")
    @WithMockUser
    void subscribeNotifications_PostMethod_Returns4xx() throws Exception {
        // given & when & then
        mockMvc.perform(post("/api/admin/notifications/stream"))
                .andExpect(status().is4xxClientError());
    }


    @Test
    @DisplayName("PUT 메소드로 SSE 구독 시도 - 4xx 에러")
    @WithMockUser
    void subscribeNotifications_PutMethod_Returns4xx() throws Exception {
        // given & when & then
        mockMvc.perform(put("/api/admin/notifications/stream"))
                .andExpect(status().is4xxClientError());
    }


    @Test
    @DisplayName("인증 없이 SSE 구독 시도 - 401 또는 403 에러")
    void subscribeNotifications_NoAuth_ReturnsUnauthorized() throws Exception {
        // given & when & then
        mockMvc.perform(get("/api/admin/notifications/stream"))
                .andExpect(status().is4xxClientError()); // 401 또는 403
    }


    @Test
    @DisplayName("SSE 엔드포인트 경로 매핑 확인")
    @WithMockUser
    void checkSseEndpointMapping() throws Exception {
        // given & when & then
        mockMvc.perform(get("/api/admin/notifications/stream"))
                .andExpect(status().isBadRequest()) // @AdminSession 없어서 400
                .andReturn();
        
        // 올바른 경로 매핑 확인됨 (404가 아닌 400 반환)
    }


    @Test
    @DisplayName("베이스 경로 확인 - /api/admin/notifications")
    @WithMockUser
    void checkBasePathMapping() throws Exception {
        // given & when & then
        mockMvc.perform(get("/api/admin/notifications"))
                .andExpect(status().is4xxClientError()); // 베이스 경로에는 매핑된 메소드 없음
    }

    @Test
    @DisplayName("SseEmitterService Bean 주입 확인")
    void sseEmitterServiceBeanInjected() {
        // given & when & then
        assert sseEmitterService != null;
    }
}