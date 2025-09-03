package com.medops.adapter.in.web.controller;

import com.medops.adapter.in.annotation.AdminSession;
import com.medops.adapter.in.web.request.*;
import com.medops.application.port.in.usecase.AdminUseCase;
import com.medops.application.port.in.command.AdminInviteCommand;
import com.medops.application.port.in.command.UpdateAdminPasswordCommand;
import com.medops.common.response.Api;
import com.medops.domain.model.Admin;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminApiController implements AdminApiControllerSpec{

    private final AdminUseCase adminUseCase;

    @Override
    @PostMapping("/login")
    public Api<String> login(@RequestBody AdminLoginRequest request){
        return Api.OK(adminUseCase.loginAdmin(request));
    }

    @Override
    @GetMapping("/me")
    public Api<Admin> me(
        @Parameter(hidden = true)
        @AdminSession Admin admin
    ){
        return Api.OK(admin);
    }

    @Override
    @PostMapping("/invite")
    public Api<Null> invite(
        @Parameter(hidden = true)
        @AdminSession Admin admin,
        @RequestBody InviteAdminRequest request
    ) {
        adminUseCase.inviteAdmin(new AdminInviteCommand(request.email(), admin.getHospital().getId()));
        return Api.OK(null);
    }

    @Override
    @PostMapping("/verify-invitation-code")
    public Api<String> verifyInvitationCode(
        @RequestBody VerifyAdminInvitationCodeRequest request
    ){
        // 중복 실행 방지 추가하기
        return Api.OK(adminUseCase.verifyInvitationCode(request));
    }

    @Override
    @PostMapping("/activate-account")
    public Api<Null> activateAccount(
        @RequestBody ActivateAdminAccountRequest request
    ){
        adminUseCase.activateAccount(request);
        return Api.OK(null);
    }

    @Override
    @PutMapping("/update-password")
    public Api<Null> updatePassword(
        @Parameter(hidden = true)
        @AdminSession Admin admin,
        @RequestBody UpdateAdminPasswordRequest request
    ){
        adminUseCase.updatePassword(
            new UpdateAdminPasswordCommand(
                admin,
                request.currentPassword(),
                request.newPassword()
            )
        );
        return Api.OK(null);
    }
}
