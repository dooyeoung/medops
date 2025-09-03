package com.medops.adapter.out.persistence.mongodb.adapter;

import com.medops.adapter.out.persistence.mongodb.converter.AdminConverter;
import com.medops.adapter.out.persistence.mongodb.document.AdminDocument;
import com.medops.adapter.out.persistence.mongodb.repository.AdminDocumentRepository;
import com.medops.application.port.out.LoadAdminPort;
import com.medops.application.port.out.SaveAdminPort;
import com.medops.domain.model.Admin;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AdminPersistenceAdapter implements SaveAdminPort, LoadAdminPort {

    private final AdminDocumentRepository adminDocumentRepository;
    private final AdminConverter adminConverter;

    @Override
    public Admin saveAdmin(Admin admin) {
        AdminDocument savedAdminDocument = adminDocumentRepository.save(
            adminConverter.toDocument(admin)
        );
        return adminConverter.toDomain(savedAdminDocument);
    }

    @Override
    public Optional<Admin> loadAdminById(String id) {
        return adminDocumentRepository.findById(id).map(adminConverter::toDomain);
    }

    @Override
    public Optional<Admin> loadAdminByEmail(String email) {
        return adminDocumentRepository.findByEmail(email).map(adminConverter::toDomain);
    }

    @Override
    public List<Admin> loadAdminsByHospitalId(String hospitalId) {
        return adminDocumentRepository.findAllByHospitalId(hospitalId).stream().map(adminConverter::toDomain).toList();
    }

    @Override
    public boolean existsByEmail(String email) {
        return adminDocumentRepository.existsByEmail(email);
    }
}
