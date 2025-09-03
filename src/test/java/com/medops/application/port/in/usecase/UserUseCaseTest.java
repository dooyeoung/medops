package com.medops.application.port.in.usecase;

import com.medops.adapter.in.web.request.UserLoginRequest;
import com.medops.application.port.out.TokenPort;
import com.medops.application.port.out.SaveUserPort;
import com.medops.domain.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserUseCaseTest {
    @Autowired
    private SaveUserPort saveUserPort;
    @Autowired
    private UserUseCase userUseCase;
    @Autowired
    private TokenPort tokenPort;

    @Test
    @DisplayName("로그인 후 /api/user/me 호출 시 사용자 정보 반환")
    void should_returnJwtWithCorrectUserId_when_loginSucceeds() {
        // given
        String testEmail = "test@test.com";
        String testPassword = "1234!@#$";
        User testUser = saveUserPort.saveUser(
            User.builder().id(UUID.randomUUID().toString()).email(testEmail).password(testPassword).build()
        );

        // when
        String jwtToken = userUseCase.loginUser(new UserLoginRequest(testEmail, testPassword));

        // then
        var userId = tokenPort.parseUserIdFromToken(jwtToken);
        assertEquals(userId, testUser.getId());
    }
}