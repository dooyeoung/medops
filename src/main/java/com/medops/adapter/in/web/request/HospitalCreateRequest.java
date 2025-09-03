package com.medops.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record HospitalCreateRequest(
    @Schema(description = "병원 이름", example = "조은의원")
    String name,
    @Schema(description = "주소", example = "강남")
    String address,
    @Schema(description = "관리자 이메일", example = "admin@gmail.com")
    String adminEmail,
    @Schema(description = "관리자 이름", example = "김실장")
    String adminName,
    @Schema(description = "관리자 비밀번호", example = "1234!@#$")
    String adminPassword
) {
}
