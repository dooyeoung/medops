package com.medops.application.port.in.usecase;

import com.medops.adapter.in.web.request.ActivateAdminAccountRequest;
import com.medops.adapter.in.web.request.AdminLoginRequest;
import com.medops.adapter.in.web.request.VerifyAdminInvitationCodeRequest;
import com.medops.adapter.out.security.adapter.JwtTokenAdapter;
import com.medops.application.dto.InvitationCodeDto;
import com.medops.application.port.in.command.AdminInviteCommand;
import com.medops.application.port.in.command.UpdateAdminPasswordCommand;
import com.medops.application.port.out.*;
import com.medops.application.service.AdminService;
import com.medops.common.exception.NotFoundResource;
import com.medops.common.exception.WrongPassword;
import com.medops.domain.enums.AdminRole;
import com.medops.domain.enums.AdminStatus;
import com.medops.domain.model.Admin;
import com.medops.domain.model.AdminRegistrationToken;
import com.medops.domain.model.Hospital;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUseCaseTest {
    @Mock private SaveAdminPort saveAdminPort;
    @Mock private LoadAdminPort loadAdminPort;
    @Mock private LoadHospitalPort loadHospitalPort;
    @Mock private VerificationCodePort verificationCodePort;
    @Mock private SaveAdminRegistrationTokenPort saveAdminRegistrationTokenPort;
    @Mock private LoadAdminRegistrationTokenPort loadAdminRegistrationTokenPort;

    private TokenPort tokenPort;
    private PasswordEncoder passwordEncoder;
    private AdminUseCase adminUseCase;

    private Admin testAdmin;
    private Hospital testHospital;
    private AdminRegistrationToken testRegistrationToken;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        tokenPort = new JwtTokenAdapter(
            "sdjfionowmiowe!@#niviwemci1238f9@SFSDFSiemiflsibisl!23gficfmbisoqofjioeF#isosdf-sfenioisemvie",
            1L
        );

        adminUseCase = new AdminService(
            passwordEncoder,
            saveAdminPort,
            loadAdminPort, 
            loadHospitalPort,
            tokenPort,
            verificationCodePort,
            saveAdminRegistrationTokenPort,
            loadAdminRegistrationTokenPort
        );
        
        lenient().when(verificationCodePort.generateVerificationCode()).thenReturn("123456");

        testHospital = Hospital.builder()
            .id("admin-1")
            .name("Test Hospital")
            .address("Test Address")
            .build();

        testAdmin = Admin.builder()
            .id("admin-1")
            .name("Test Admin")
            .email("admin@test.com")
            .password(passwordEncoder.encode("password123"))
            .role(AdminRole.MANAGER)
            .status(AdminStatus.ACTIVATED)
            .hospital(testHospital)
            .build();

        testRegistrationToken = AdminRegistrationToken.builder()
            .id("token-1")
            .adminId("admin-1")
            .registrationToken("registration-token-123")
            .registrationTokenExpiresAt(LocalDateTime.now().plusHours(24))
            .build();
    }

    @Test
    @DisplayName("어드민 로그인 성공시 JWT 토큰 반환")
    void should_returnJwtToken_when_adminLoginSucceeds() {
        // given
        when(loadAdminPort.loadAdminByEmail("admin@test.com")).thenReturn(Optional.of(testAdmin));

        // when
        AdminLoginRequest request = new AdminLoginRequest("admin@test.com", "password123", "Test Hospital");
        String result = adminUseCase.loginAdmin(request);

        // then
        assertNotNull(result);
        verify(loadAdminPort).loadAdminByEmail("admin@test.com");
    }

    @Test
    @DisplayName("존재하지 않는 어드민 로그인시 NotFoundResource 예외 발생")
    void should_throwNotFoundResource_when_adminNotExists() {
        // given
        AdminLoginRequest request = new AdminLoginRequest("nonexistent@test.com", "password123", "Test Hospital");
        // when(loadAdminPort.loadAdminByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundResource.class, () -> adminUseCase.loginAdmin(request));
        verify(loadAdminPort).loadAdminByEmail("nonexistent@test.com");
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인시 WrongPassword 예외 발생")
    void should_throwWrongPassword_when_wrongPassword() {
        // given
        AdminLoginRequest request = new AdminLoginRequest("admin@test.com", "wrongPassword", "Test Hospital");
        when(loadAdminPort.loadAdminByEmail("admin@test.com")).thenReturn(Optional.of(testAdmin));

        // when & then
        assertThrows(WrongPassword.class, () -> adminUseCase.loginAdmin(request));
        verify(loadAdminPort).loadAdminByEmail("admin@test.com");
    }

    @Test
    @DisplayName("어드민 초대시 검증 코드 생성 및 저장")
    void should_generateAndSaveVerificationCode_when_inviteAdmin() {
        // given
        AdminInviteCommand command = new AdminInviteCommand("newadmin@test.com", "hospital-1");
        String generatedCode = "123456";
        
        when(verificationCodePort.generateVerificationCode()).thenReturn(generatedCode);

        // when
        adminUseCase.inviteAdmin(command);

        // then
        verify(verificationCodePort).generateVerificationCode();
        verify(verificationCodePort).saveVerificationCode("newadmin@test.com", generatedCode, "hospital-1");
    }

    @Test
    @DisplayName("올바른 초대 코드 검증시 어드민 생성 및 등록 토큰 반환")
    void should_createAdminAndReturnRegistrationToken_when_validInvitationCode() {
        // given
        String email = "newadmin@test.com";
        String verificationCode = "123456";
        String expectedRegistrationToken = "registration-token-456";
        
        VerifyAdminInvitationCodeRequest request = new VerifyAdminInvitationCodeRequest(email, verificationCode);
        InvitationCodeDto invitationCodeDto = new InvitationCodeDto(verificationCode, "hospital-1");
        
        Admin newAdmin = Admin.builder()
            .id("new-admin-id")
            .name("신규어드민")
            .email(email)
            .password("1234!@#$")
            .role(AdminRole.MANAGER)
            .status(AdminStatus.INVITED)
            .hospital(testHospital)
            .build();

        when(verificationCodePort.getInvitationCode(email)).thenReturn(invitationCodeDto);
        when(loadHospitalPort.loadHospitalById("hospital-1")).thenReturn(Optional.of(testHospital));
        when(saveAdminPort.saveAdmin(any(Admin.class))).thenReturn(newAdmin);
        when(saveAdminRegistrationTokenPort.createAdminRegistrationToken(newAdmin)).thenReturn(expectedRegistrationToken);

        // when
        String result = adminUseCase.verifyInvitationCode(request);

        // then
        assertEquals(expectedRegistrationToken, result);
        verify(verificationCodePort).getInvitationCode(email);
        verify(verificationCodePort).removeVerificationCode(email);
        verify(loadHospitalPort).loadHospitalById("hospital-1");
        verify(saveAdminPort).saveAdmin(any(Admin.class));
        verify(saveAdminRegistrationTokenPort).createAdminRegistrationToken(newAdmin);
    }

    @Test
    @DisplayName("잘못된 초대 코드 검증시 예외 발생")
    void should_throwException_when_invalidInvitationCode() {
        // given
        String email = "newadmin@test.com";
        String correctCode = "123456";
        String wrongCode = "999999";
        
        VerifyAdminInvitationCodeRequest request = new VerifyAdminInvitationCodeRequest(email, wrongCode);
        InvitationCodeDto invitationCodeDto = new InvitationCodeDto(correctCode, "hospital-1");
        
        when(verificationCodePort.getInvitationCode(email)).thenReturn(invitationCodeDto);

        // when & then
        assertThrows(IllegalArgumentException.class, () -> adminUseCase.verifyInvitationCode(request));
        verify(verificationCodePort).getInvitationCode(email);
        verifyNoInteractions(loadHospitalPort, saveAdminPort, saveAdminRegistrationTokenPort);
    }

    @Test
    @DisplayName("존재하지 않는 병원 ID로 초대 코드 검증시 예외 발생")
    void should_throwException_when_hospitalNotFound() {
        // given
        String email = "newadmin@test.com";
        String verificationCode = "123456";
        
        VerifyAdminInvitationCodeRequest request = new VerifyAdminInvitationCodeRequest(email, verificationCode);
        InvitationCodeDto invitationCodeDto = new InvitationCodeDto(verificationCode, "nonexistent-hospital");
        
        when(verificationCodePort.getInvitationCode(email)).thenReturn(invitationCodeDto);
        when(loadHospitalPort.loadHospitalById("nonexistent-hospital")).thenReturn(Optional.empty());

        // when & then
        assertThrows(IllegalArgumentException.class, () -> adminUseCase.verifyInvitationCode(request));
        verify(verificationCodePort).removeVerificationCode(email);
        verify(loadHospitalPort).loadHospitalById("nonexistent-hospital");
        verifyNoInteractions(saveAdminPort, saveAdminRegistrationTokenPort);
    }

    @Test
    @DisplayName("계정 활성화 성공시 어드민 정보 및 토큰 상태 업데이트")
    void should_activateAdminAndUpdateToken_when_validActivationRequest() {
        // given
        Admin invitedAdmin = Admin.builder()
            .id("admin-1")
            .name("Test Admin")
            .email("admin@test.com")
            .password("tempPassword")
            .role(AdminRole.MANAGER)
            .status(AdminStatus.INVITED)
            .hospital(testHospital)
            .build();
        
        AdminRegistrationToken validToken = AdminRegistrationToken.builder()
            .id("token-1")
            .adminId("admin-1")
            .registrationToken("registration-token-123")
            .registrationTokenExpiresAt(LocalDateTime.now().plusHours(24))
            .registeredAt(null)
            .build();
        
        ActivateAdminAccountRequest request = new ActivateAdminAccountRequest(
            "admin@test.com", "ActiveAdmin", "newPassword123", "registration-token-123"
        );

        when(loadAdminPort.loadAdminByEmail("admin@test.com")).thenReturn(Optional.of(invitedAdmin));
        when(loadAdminRegistrationTokenPort.loadAdminRegistrationTokenByAdminId("admin-1"))
            .thenReturn(Optional.of(validToken));

        // when
        adminUseCase.activateAccount(request);

        // then
        verify(loadAdminPort).loadAdminByEmail("admin@test.com");
        verify(loadAdminRegistrationTokenPort).loadAdminRegistrationTokenByAdminId("admin-1");
        verify(saveAdminRegistrationTokenPort).updateAdminRegistrationToken(validToken);
        verify(saveAdminPort).saveAdmin(invitedAdmin);
    }

    @Test
    @DisplayName("존재하지 않는 어드민 계정 활성화시 예외 발생")
    void should_throwNotFoundResource_when_adminNotExistsForActivation() {
        // given
        ActivateAdminAccountRequest request = new ActivateAdminAccountRequest(
            "nonexistent@test.com", "Name", "password", "token"
        );
        when(loadAdminPort.loadAdminByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundResource.class, () -> adminUseCase.activateAccount(request));
        verify(loadAdminPort).loadAdminByEmail("nonexistent@test.com");
        verifyNoInteractions(loadAdminRegistrationTokenPort, saveAdminRegistrationTokenPort, saveAdminPort);
    }

    @Test
    @DisplayName("등록 토큰이 없는 경우 계정 활성화시 예외 발생")
    void should_throwNotFoundResource_when_registrationTokenNotExists() {
        // given
        ActivateAdminAccountRequest request = new ActivateAdminAccountRequest(
            "admin@test.com", "Name", "password", "token"
        );
        
        when(loadAdminPort.loadAdminByEmail("admin@test.com")).thenReturn(Optional.of(testAdmin));
        when(loadAdminRegistrationTokenPort.loadAdminRegistrationTokenByAdminId("admin-1"))
            .thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundResource.class, () -> adminUseCase.activateAccount(request));
        verify(loadAdminPort).loadAdminByEmail("admin@test.com");
        verify(loadAdminRegistrationTokenPort).loadAdminRegistrationTokenByAdminId("admin-1");
        verifyNoInteractions(saveAdminRegistrationTokenPort, saveAdminPort);
    }

    @Test
    @DisplayName("이미 사용된 등록 토큰으로 계정 활성화시 예외 발생")
    void should_throwException_when_registrationTokenAlreadyUsed() {
        // given
        AdminRegistrationToken usedToken = AdminRegistrationToken.builder()
            .id("token-1")
            .adminId("admin-1")
            .registrationToken("registration-token-123")
            .registrationTokenExpiresAt(LocalDateTime.now().plusHours(24))
            .registeredAt(LocalDateTime.now())
            .build();
        
        ActivateAdminAccountRequest request = new ActivateAdminAccountRequest(
            "admin@test.com", "ActiveAdmin", "newPassword123", "registration-token-123"
        );

        when(loadAdminPort.loadAdminByEmail("admin@test.com")).thenReturn(Optional.of(testAdmin));
        when(loadAdminRegistrationTokenPort.loadAdminRegistrationTokenByAdminId("admin-1"))
            .thenReturn(Optional.of(usedToken));

        // when & then
        assertThrows(IllegalArgumentException.class, () -> adminUseCase.activateAccount(request));
        verify(loadAdminPort).loadAdminByEmail("admin@test.com");
        verify(loadAdminRegistrationTokenPort).loadAdminRegistrationTokenByAdminId("admin-1");
        verifyNoInteractions(saveAdminRegistrationTokenPort, saveAdminPort);
    }

    @Test
    @DisplayName("잘못된 등록 토큰으로 계정 활성화시 예외 발생")
    void should_throwException_when_wrongRegistrationToken() {
        // given
        AdminRegistrationToken validToken = AdminRegistrationToken.builder()
            .id("token-1")
            .adminId("admin-1")
            .registrationToken("registration-token-123")
            .registrationTokenExpiresAt(LocalDateTime.now().plusHours(24))
            .registeredAt(null)
            .build();
        
        ActivateAdminAccountRequest request = new ActivateAdminAccountRequest(
            "admin@test.com", "ActiveAdmin", "newPassword123", "wrong-token"
        );

        when(loadAdminPort.loadAdminByEmail("admin@test.com")).thenReturn(Optional.of(testAdmin));
        when(loadAdminRegistrationTokenPort.loadAdminRegistrationTokenByAdminId("admin-1"))
            .thenReturn(Optional.of(validToken));

        // when & then
        assertThrows(IllegalArgumentException.class, () -> adminUseCase.activateAccount(request));
        verify(loadAdminPort).loadAdminByEmail("admin@test.com");
        verify(loadAdminRegistrationTokenPort).loadAdminRegistrationTokenByAdminId("admin-1");
        verifyNoInteractions(saveAdminRegistrationTokenPort, saveAdminPort);
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    void should_updatePassword_when_correctCurrentPassword() {
        // given
        UpdateAdminPasswordCommand command = new UpdateAdminPasswordCommand(
            testAdmin, "password123", "newPassword456"
        );
        System.out.println(testAdmin.getPassword());

        // when
        adminUseCase.updatePassword(command);

        // then
        verify(saveAdminPort).saveAdmin(testAdmin);
    }

    @Test
    @DisplayName("잘못된 현재 비밀번호로 변경시 예외 발생")
    void should_throwException_when_wrongCurrentPassword() {
        // given
        UpdateAdminPasswordCommand command = new UpdateAdminPasswordCommand(
            testAdmin, "wrongPassword", "newPassword456"
        );

        // when & then
        assertThrows(IllegalArgumentException.class, () -> adminUseCase.updatePassword(command));
        verifyNoInteractions(saveAdminPort);
    }
}