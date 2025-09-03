package com.medops.adapter.out.persistence.mongodb.adapter;

import com.medops.adapter.out.persistence.mongodb.converter.AdminRegistrationTokenConverter;
import com.medops.adapter.out.persistence.mongodb.document.AdminRegistrationTokenDocument;
import com.medops.adapter.out.persistence.mongodb.repository.AdminRegistrationTokenDocumentRepository;
import com.medops.application.port.out.LoadAdminRegistrationTokenPort;
import com.medops.application.port.out.SaveAdminRegistrationTokenPort;
import com.medops.domain.model.Admin;
import com.medops.domain.model.AdminRegistrationToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AdminRegistrationTokenPersistenceAdapter implements SaveAdminRegistrationTokenPort, LoadAdminRegistrationTokenPort {
    private final AdminRegistrationTokenDocumentRepository adminRegistrationTokenDocumentRepository;
    private final AdminRegistrationTokenConverter adminRegistrationTokenConverter;


    @Override
    public String createAdminRegistrationToken(Admin admin) {
        AdminRegistrationTokenDocument savedDocument = adminRegistrationTokenDocumentRepository.save(
            AdminRegistrationTokenDocument.builder()
                .adminId(admin.getId())
                .registrationToken(UUID.randomUUID().toString())
                .registrationTokenExpiresAt(LocalDateTime.now().plusMinutes(15))
                .build()
        );
        return savedDocument.getRegistrationToken();
    }

    @Override
    public void updateAdminRegistrationToken(AdminRegistrationToken adminRegistrationToken) {
        adminRegistrationTokenDocumentRepository.save(
            adminRegistrationTokenConverter.toDocument(adminRegistrationToken)
        );
    }

    @Override
    public Optional<AdminRegistrationToken> loadAdminRegistrationTokenByAdminId(String adminId) {
        return adminRegistrationTokenDocumentRepository.findByAdminId(adminId).map(adminRegistrationTokenConverter::toDomain);
    }
}
