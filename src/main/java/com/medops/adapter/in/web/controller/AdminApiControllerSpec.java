package com.medops.adapter.in.web.controller;

import com.medops.adapter.in.web.request.*;
import com.medops.common.response.Api;
import com.medops.domain.model.Admin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Null;

@Tag(name = "관리자 API")
public interface AdminApiControllerSpec {
    @Operation(
        summary = "관리자 로그인",
        requestBody = @RequestBody(
            content = {
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AdminLoginRequest.class)
                )
            }
        )
    )
    Api<String> login(AdminLoginRequest request);

    @Operation(summary = "내 정보 조회")
    Api<Admin> me(Admin admin);

    @Operation(
        summary = "관리자 초대",
        requestBody = @RequestBody(
            content = {
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = InviteAdminRequest.class)
                )
            }
        )
    )
    Api<Null> invite(Admin admin, InviteAdminRequest request);


    @Operation(
        summary = "인증번호 검증",
        requestBody = @RequestBody(
            content = {
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = VerifyAdminInvitationCodeRequest.class)
                )
            }
        )
    )
    Api<String> verifyInvitationCode(VerifyAdminInvitationCodeRequest request);

    @Operation(
        summary = "계정 활성화",
        requestBody = @RequestBody(
            content = {
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ActivateAdminAccountRequest.class)
                )
            }
        )
    )
    Api<Null> activateAccount(ActivateAdminAccountRequest request);

    @Operation(
        summary = "비밀번호 변경",
        requestBody = @RequestBody(
            content = {
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UpdateAdminPasswordRequest.class)
                )
            }
        )
    )
    Api<Null> updatePassword(Admin admin, UpdateAdminPasswordRequest request);
}
