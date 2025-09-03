package com.medops.adapter.out.persistence.mongodb.converter;

import com.medops.adapter.out.persistence.mongodb.document.AdminDocument;
import com.medops.application.port.out.LoadHospitalPort;
import com.medops.domain.model.Admin;
import com.medops.domain.model.Hospital;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminConverter {

    private final LoadHospitalPort loadHospitalPort;

    public AdminDocument toDocument(Admin admin){
        return new AdminDocument(
            admin.getId(),
            admin.getName(),
            admin.getEmail(),
            admin.getPassword(),
            admin.getRole(),
            admin.getStatus(),
            admin.getHospital().getId(),
            admin.getCreatedAt(),
            admin.getDeletedAt()
        );
    }

    public Admin toDomain(AdminDocument adminDocument) {
        Hospital hospital = loadHospitalPort.loadHospitalById(
            adminDocument.getHospitalId()
        ).orElseThrow(IllegalAccessError::new);

        return Admin.builder()
            .id(adminDocument.getId())
            .name(adminDocument.getName())
            .email(adminDocument.getEmail())
            .password(adminDocument.getPassword())
            .role(adminDocument.getRole())
            .status(adminDocument.getStatus())
            .hospital(hospital)
            .createdAt(adminDocument.getCreatedAt())
            .deletedAt(adminDocument.getDeletedAt())
            .build();
    }
}
