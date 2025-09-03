package com.medops.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record AdminLoginRequest(
    @Schema(description = "어드민 email", example = "admin@gmail.com")
    String email,
    @Schema(description = "어드민 password", example = "1234!@#$")
    String password,
    @Schema(description = "소속 병원 이름", example = "조은의원")
    String hospitalName
) {
}
