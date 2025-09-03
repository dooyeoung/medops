package com.medops.adapter.out.persistence.mongodb.converter;

import com.medops.adapter.out.persistence.mongodb.document.HospitalDocument;
import com.medops.domain.model.Hospital;
import org.springframework.stereotype.Component;

@Component
public class HospitalConverter {

    public HospitalDocument toDocument(Hospital hospital){
        return new HospitalDocument(
            hospital.getId(),
            hospital.getName(),
            hospital.getAddress(),
            hospital.getCreatedAt()
        );
    }
    public Hospital toDomain(HospitalDocument document) {
        return Hospital.builder()
            .id(document.getId())
            .name(document.getName())
            .address(document.getAddress())
            .createdAt(document.getCreatedAt())
            .build();
    }
}
