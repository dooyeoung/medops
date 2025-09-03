package com.medops.adapter.out.persistence.mongodb.converter;

import com.medops.adapter.out.persistence.mongodb.document.TreatmentProductDocument;
import com.medops.domain.model.TreatmentProduct;
import org.springframework.stereotype.Component;

@Component
public class TreatmentProductConverter {
    public TreatmentProductDocument toDocument(TreatmentProduct treatmentProduct){
        return new TreatmentProductDocument(
            treatmentProduct.getId(),
            treatmentProduct.getHospitalId(),
            treatmentProduct.getName(),
            treatmentProduct.getDescription(),
            treatmentProduct.getMaxCapacity(),
            treatmentProduct.getPrice(),
            treatmentProduct.getCreatedAt(),
            treatmentProduct.getDeletedAt()
        );
    }

    public TreatmentProduct toDomain(TreatmentProductDocument treatmentProductDocument){
        return TreatmentProduct.builder()
            .id(treatmentProductDocument.getId())
            .hospitalId(treatmentProductDocument.getHospitalId())
            .name(treatmentProductDocument.getName())
            .description(treatmentProductDocument.getDescription())
            .maxCapacity(treatmentProductDocument.getMaxCapacity())
            .price(treatmentProductDocument.getPrice())
            .createdAt(treatmentProductDocument.getCreatedAt())
            .deletedAt(treatmentProductDocument.getDeletedAt())
            .build();
    }
}
