package com.medops.adapter.out.persistence.mongodb.adapter;

import com.medops.adapter.out.persistence.mongodb.converter.BusinessHourConverter;
import com.medops.adapter.out.persistence.mongodb.document.BusinessHourDocument;
import com.medops.adapter.out.persistence.mongodb.repository.BusinessHourDocumentRepository;
import com.medops.application.port.out.LoadBusinessHourPort;
import com.medops.application.port.out.SaveBusinessHourPort;
import com.medops.domain.model.BusinessHour;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BusinessHourPersistenceAdapter implements SaveBusinessHourPort, LoadBusinessHourPort {

    private final BusinessHourDocumentRepository businessHourDocumentRepository;
    private final BusinessHourConverter businessHourConverter;


    @Override
    public BusinessHour saveBusinessHour(BusinessHour businessHour) {
        BusinessHourDocument savedBusinessHourDocument = businessHourDocumentRepository.save(
            businessHourConverter.toDocument(businessHour)
        );
        return businessHourConverter.toDomain(savedBusinessHourDocument);
    }

    @Override
    public List<BusinessHour> loadBusinessHoursByHospitalId(String HospitalId) {
        return businessHourDocumentRepository.findAllByHospitalId(HospitalId).stream().map(
            businessHourConverter::toDomain
        ).toList();
    }

    @Override
    public Optional<BusinessHour> loadBusinessHourById(String businessHourId) {
        return businessHourDocumentRepository.findById(businessHourId).map(businessHourConverter::toDomain);
    }
}
