package com.medops.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record InviteAdminRequest(
    @Schema(description = "어드민 email", example = "admin@gmail.com")
    String email
) {
}
