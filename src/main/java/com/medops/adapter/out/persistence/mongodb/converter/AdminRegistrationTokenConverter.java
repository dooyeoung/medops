package com.medops.adapter.out.persistence.mongodb.converter;

import com.medops.adapter.out.persistence.mongodb.document.AdminRegistrationTokenDocument;
import com.medops.domain.model.AdminRegistrationToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminRegistrationTokenConverter {
    public AdminRegistrationTokenDocument toDocument(AdminRegistrationToken adminRegistrationToken){
        return new AdminRegistrationTokenDocument(
            adminRegistrationToken.getId(),
            adminRegistrationToken.getAdminId(),
            adminRegistrationToken.getRegistrationToken(),
            adminRegistrationToken.getRegistrationTokenExpiresAt(),
            adminRegistrationToken.getRegisteredAt()
        );
    }

    public AdminRegistrationToken toDomain(AdminRegistrationTokenDocument adminRegistrationTokenDocument) {
        return AdminRegistrationToken.builder()
            .id(adminRegistrationTokenDocument.getId())
            .adminId(adminRegistrationTokenDocument.getAdminId())
            .registrationToken(adminRegistrationTokenDocument.getRegistrationToken())
            .registrationTokenExpiresAt(adminRegistrationTokenDocument.getRegistrationTokenExpiresAt())
            .registeredAt(adminRegistrationTokenDocument.getRegisteredAt())
            .build();
    }
}
