package com.medops.adapter.out.persistence.mongodb.converter;

import com.medops.adapter.out.persistence.mongodb.document.DoctorDocument;
import com.medops.domain.model.Doctor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DoctorConverter {

    public DoctorDocument toDocument(Doctor doctor){
        return new DoctorDocument(
            doctor.getId(),
            doctor.getHospitalId(),
            doctor.getName(),
            doctor.getCreatedAt(),
            doctor.getDeletedAt()
        );
    }

    public Doctor toDomain(DoctorDocument doctorDocument) {
        return Doctor.builder()
            .id(doctorDocument.getId())
            .name(doctorDocument.getName())
            .hospitalId(doctorDocument.getHospitalId())
            .createdAt(doctorDocument.getCreatedAt())
            .deletedAt(doctorDocument.getDeletedAt())
            .build();
    }
}
