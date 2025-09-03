package com.medops.application.service;

import com.medops.adapter.in.web.request.ActivateAdminAccountRequest;
import com.medops.adapter.in.web.request.AdminLoginRequest;
import com.medops.adapter.in.web.request.VerifyAdminInvitationCodeRequest;
import com.medops.application.port.in.command.AdminInviteCommand;
import com.medops.application.port.in.command.UpdateAdminPasswordCommand;
import com.medops.application.port.out.*;
import com.medops.application.dto.InvitationCodeDto;
import com.medops.common.exception.NotFoundResource;
import com.medops.common.exception.WrongPassword;
import com.medops.domain.model.AdminRegistrationToken;
import com.medops.domain.model.Hospital;
import com.medops.domain.enums.AdminRole;
import com.medops.domain.enums.AdminStatus;
import com.medops.domain.enums.TokenType;
import com.medops.application.port.in.usecase.AdminUseCase;
import com.medops.domain.model.Admin;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService implements AdminUseCase {
    private final PasswordEncoder passwordEncoder;

    private final SaveAdminPort saveAdminPort;
    private final LoadAdminPort loadAdminPort;
    private final LoadHospitalPort loadHospitalPort;
    private final TokenPort tokenPort;
    private final VerificationCodePort verificationCodePort;
    private final SaveAdminRegistrationTokenPort saveAdminRegistrationTokenPort;
    private final LoadAdminRegistrationTokenPort loadAdminRegistrationTokenPort;

    @Override
    public String loginAdmin(AdminLoginRequest request) {
        Admin admin = loadAdminPort.loadAdminByEmail(request.email()).orElseThrow(() -> new NotFoundResource("사용자 찾을수 없음"));
        if (!passwordEncoder.matches(request.password(), admin.getPassword())){
            throw new WrongPassword();
        }

        return tokenPort.generateToken(admin.getId(), TokenType.ADMIN);
    }

    @Override
    public void inviteAdmin(AdminInviteCommand command) {
        String code = verificationCodePort.generateVerificationCode();
        verificationCodePort.saveVerificationCode(
            command.email(), code, command.hospitalId()
        );
        System.out.println(code);
    }

    @Override
    public String verifyInvitationCode(VerifyAdminInvitationCodeRequest request) {
        InvitationCodeDto invitationCode = verificationCodePort.getInvitationCode(request.email());
        if (request.code().equals(invitationCode.code())){
            verificationCodePort.removeVerificationCode(request.email());

            Hospital hospital = loadHospitalPort.loadHospitalById(invitationCode.hospitalId()).orElseThrow(() -> new IllegalArgumentException("초대한 병원 정보가 누락되었습니다"));
            Admin savedAdmin = saveAdminPort.saveAdmin(
                Admin.builder()
                    .id(UUID.randomUUID().toString())
                    .name("신규어드민")
                    .email(request.email())
                    .password(passwordEncoder.encode("1234!@#$"))
                    .role(AdminRole.MANAGER)
                    .status(AdminStatus.INVITED)
                    .hospital(hospital)
                    .createdAt(Instant.now())
                    .deletedAt(null)
                    .build()
            );
            // 검증 완료후 캐시 지우기
            return saveAdminRegistrationTokenPort.createAdminRegistrationToken(savedAdmin);
        }

        throw new IllegalArgumentException("잘못된 코드입니다");
    }

    @Override
    public void activateAccount(ActivateAdminAccountRequest request) {
        // 가입 검증해야할 어드민 없는 경우
        Admin admin = loadAdminPort.loadAdminByEmail(request.adminEmail()).orElseThrow(
            () -> new NotFoundResource("관리자를 찾을수 없습니다.")
        );

        // 가입 토큰 없는 경우
        AdminRegistrationToken registrationToken = loadAdminRegistrationTokenPort.loadAdminRegistrationTokenByAdminId(admin.getId()).orElseThrow(
            () -> new NotFoundResource("관리자 초대가 진행되지 않았습니다.")
        );

        if(registrationToken.getRegisteredAt() != null){
            throw new IllegalArgumentException("이미 사용된 가입 토큰 입니다.");
        }

        // 토큰이 잘못 전달 된 경우
        if(!request.registrationToken().equals(registrationToken.getRegistrationToken())){
            throw new IllegalArgumentException("잘못된 접근입니다.");
        }

        registrationToken.completeRegister();
        saveAdminRegistrationTokenPort.updateAdminRegistrationToken(registrationToken);

        admin.activateAccount(
            request.adminName(), passwordEncoder.encode(request.password())
        );
        saveAdminPort.saveAdmin(admin);
    }

    @Override
    public void updatePassword(UpdateAdminPasswordCommand command) {
        Admin admin = command.admin();
        System.out.println(admin.getPassword());
        System.out.println(command.currentPassword());
        if (!passwordEncoder.matches(command.currentPassword(), admin.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호 다름");
        }

        String encodedPassword = passwordEncoder.encode(command.newPassword());
        admin.setPassword(encodedPassword);
        saveAdminPort.saveAdmin(admin);
    }

    @Override
    public List<Admin> getAdminsByHospitalId(String hospitalId) {
        return loadAdminPort.loadAdminsByHospitalId(hospitalId);
    }
}
