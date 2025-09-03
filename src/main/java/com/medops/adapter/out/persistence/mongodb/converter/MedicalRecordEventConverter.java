
package com.medops.adapter.out.persistence.mongodb.converter;

import com.medops.adapter.out.persistence.mongodb.document.MedicalRecordEventDocument;
import com.medops.domain.event.MedicalRecordEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MedicalRecordEventConverter {

    public MedicalRecordEvent toDomain(MedicalRecordEventDocument document) {
        if (document == null) {
            return null;
        }
        return MedicalRecordEvent.builder()
            .id(document.getId())
            .recordId(document.getRecordId())
            .createdAt(document.getCreatedAt())
            .eventType(document.getEventType())
            .hospitalId(document.getHospitalId())
            .userId(document.getUserId())
            .version(document.getVersion())
            .status(document.getStatus())
            .payload(document.getPayload())
            .build();
    }

    public List<MedicalRecordEvent> toDomainList(List<MedicalRecordEventDocument> documents) {
        if (documents == null) {
            return List.of();
        }
        return documents.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    public MedicalRecordEventDocument toDocument(MedicalRecordEvent domainModel) {
        if (domainModel == null) {
            return null;
        }
        MedicalRecordEventDocument document = new MedicalRecordEventDocument(
            domainModel.getId(),
            domainModel.getRecordId(),
            domainModel.getCreatedAt(),
            domainModel.getEventType(),
            domainModel.getHospitalId(),
            domainModel.getUserId(),
            domainModel.getVersion(),
            domainModel.getStatus(),
            domainModel.getPayload()
        );
        return document;
    }
}
