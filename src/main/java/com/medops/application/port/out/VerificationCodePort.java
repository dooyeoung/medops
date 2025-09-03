package com.medops.application.port.out;

import com.medops.application.dto.InvitationCodeDto;

public interface VerificationCodePort {
    String generateVerificationCode();
    void saveVerificationCode(String email, String code, String hospitalId);
    InvitationCodeDto getInvitationCode(String email);
    void removeVerificationCode(String email);
}
