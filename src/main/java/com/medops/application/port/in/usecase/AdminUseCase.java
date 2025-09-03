package com.medops.application.port.in.usecase;

import com.medops.adapter.in.web.request.ActivateAdminAccountRequest;
import com.medops.adapter.in.web.request.AdminLoginRequest;
import com.medops.adapter.in.web.request.VerifyAdminInvitationCodeRequest;
import com.medops.application.port.in.command.AdminInviteCommand;
import com.medops.application.port.in.command.UpdateAdminPasswordCommand;
import com.medops.domain.model.Admin;

import java.util.List;

public interface AdminUseCase {

    String loginAdmin(AdminLoginRequest request);
    void inviteAdmin(AdminInviteCommand command);
    String verifyInvitationCode(VerifyAdminInvitationCodeRequest request);
    void activateAccount(ActivateAdminAccountRequest request);
    void updatePassword(UpdateAdminPasswordCommand command);
    List<Admin> getAdminsByHospitalId(String hospitalId);
}
