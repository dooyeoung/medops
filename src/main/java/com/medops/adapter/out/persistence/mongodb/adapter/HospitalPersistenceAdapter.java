package com.medops.adapter.out.persistence.mongodb.adapter;

import com.medops.adapter.out.persistence.mongodb.converter.HospitalConverter;
import com.medops.adapter.out.persistence.mongodb.document.HospitalDocument;
import com.medops.adapter.out.persistence.mongodb.repository.HospitalDocumentRepository;
import com.medops.application.port.out.LoadHospitalPort;
import com.medops.application.port.out.SaveHospitalPort;
import com.medops.domain.model.Hospital;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class HospitalPersistenceAdapter implements SaveHospitalPort, LoadHospitalPort {
    private final HospitalDocumentRepository hospitalDocumentRepository;
    private final HospitalConverter hospitalConverter;

    @Override
    public Hospital saveHospital(Hospital hospital) {
        HospitalDocument savedDocument = hospitalDocumentRepository.save(
            hospitalConverter.toDocument(hospital)
        );
        return hospitalConverter.toDomain(savedDocument);
    }

    @Override
    public Optional<Hospital> loadHospitalById(String id) {
        return hospitalDocumentRepository.findById(id).map(hospitalConverter::toDomain);
    }

    @Override
    public Optional<Hospital> loadHospitalByName(String name) {
        return hospitalDocumentRepository.findByName(name).map(hospitalConverter::toDomain);
    }

    @Override
    public List<Hospital> loadAllHospitals() {
        return hospitalDocumentRepository.findAll().stream().map(hospitalConverter::toDomain).toList();
    }

    @Override
    public boolean existsByName(String name) {
        return hospitalDocumentRepository.existsByName(name);
    }
}
